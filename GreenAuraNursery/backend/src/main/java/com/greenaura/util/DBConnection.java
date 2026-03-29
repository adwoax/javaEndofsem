package com.greenaura.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Utility class for PostgreSQL connectivity and one-time schema bootstrap. */
public class DBConnection {

    private static final Map<String, String> FILE_CONFIG = loadEnvFileConfig();
    private static final boolean PREFER_SUPABASE_CREDENTIALS = shouldPreferSupabaseCredentials();

    // Override on each device/server using environment variables.
    // Priority order:
    // 1) GREENAURA_DB_URL / GREENAURA_DB_USER / GREENAURA_DB_PASSWORD
    // 2) SUPABASE_DB_HOST / SUPABASE_DB_PORT / SUPABASE_DB_NAME / SUPABASE_DB_USER / SUPABASE_DB_PASSWORD
    private static final String URL = resolveJdbcUrl();
    private static final String USERNAME =
        PREFER_SUPABASE_CREDENTIALS
            ? firstNonBlank(
                getConfig("SUPABASE_DB_USER"),
                getConfig("GREENAURA_DB_USER"),
                "postgres"
            )
            : firstNonBlank(
                getConfig("GREENAURA_DB_USER"),
                getConfig("SUPABASE_DB_USER"),
                "postgres"
            );
    private static final String PASSWORD =
        PREFER_SUPABASE_CREDENTIALS
            ? firstNonBlank(
                getConfig("SUPABASE_DB_PASSWORD"),
                getConfig("GREENAURA_DB_PASSWORD"),
                "postgres"
            )
            : firstNonBlank(
                getConfig("GREENAURA_DB_PASSWORD"),
                getConfig("SUPABASE_DB_PASSWORD"),
                "postgres"
            );

    private static volatile boolean initialized = false;
    private static volatile boolean configLogged = false;

    /** Returns a live PostgreSQL connection and ensures bootstrap has run. */
    public static Connection getConnection() {
        logResolvedConfigOnce();
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            initializeIfNeeded(conn);
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: PostgreSQL JDBC Driver not found.");
            System.out.println("Fix: Ensure PostgreSQL dependency exists in pom.xml.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("ERROR: Could not connect to the database.");
            System.out.println("Fix: Check GREENAURA_DB_URL/GREENAURA_DB_USER/GREENAURA_DB_PASSWORD or SUPABASE_DB_* variables.");
            e.printStackTrace();
        }
        return null;
    }

    private static void logResolvedConfigOnce() {
        if (configLogged) return;
        synchronized (DBConnection.class) {
            if (configLogged) return;
            System.out.println("DB URL resolved to: " + URL);
            System.out.println("DB USER resolved to: " + USERNAME);
            configLogged = true;
        }
    }

    private static String resolveJdbcUrl() {
        String directUrl = firstNonBlank(getConfig("GREENAURA_DB_URL"), null);
        String host = firstNonBlank(getConfig("SUPABASE_DB_HOST"), null);
        String port = firstNonBlank(getConfig("SUPABASE_DB_PORT"), "5432");
        String dbName = firstNonBlank(getConfig("SUPABASE_DB_NAME"), "postgres");
        String sslMode = firstNonBlank(getConfig("GREENAURA_DB_SSLMODE"), "require");

        if (directUrl != null) {
            String normalizedDirectUrl = normalizeJdbcUrl(directUrl);
            // If a stale local URL is set globally, prefer Supabase config when present.
            if (!shouldPreferSupabaseOverDirectUrl(normalizedDirectUrl, host)) {
                return normalizedDirectUrl;
            }
        }

        if (host != null) {
            return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=" + sslMode;
        }

        return "jdbc:postgresql://localhost:5432/green_aura_nursery";
    }

    private static String normalizeJdbcUrl(String rawUrl) {
        String trimmed = rawUrl.trim();
        if (trimmed.startsWith("jdbc:postgresql://")) {
            return appendDefaultSslMode(trimmed);
        }
        if (trimmed.startsWith("postgresql://")) {
            return appendDefaultSslMode("jdbc:" + trimmed);
        }
        if (trimmed.startsWith("postgres://")) {
            return appendDefaultSslMode("jdbc:postgresql://" + trimmed.substring("postgres://".length()));
        }
        return appendDefaultSslMode(trimmed);
    }

    private static String appendDefaultSslMode(String jdbcUrl) {
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        String sslMode = firstNonBlank(getConfig("GREENAURA_DB_SSLMODE"), "require");
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=" + sslMode;
    }

    private static boolean shouldPreferSupabaseCredentials() {
        String host = firstNonBlank(getConfig("SUPABASE_DB_HOST"), null);
        String directUrl = firstNonBlank(getConfig("GREENAURA_DB_URL"), null);
        if (host == null || directUrl == null) {
            return false;
        }
        return shouldPreferSupabaseOverDirectUrl(normalizeJdbcUrl(directUrl), host);
    }

    private static boolean shouldPreferSupabaseOverDirectUrl(String normalizedDirectUrl, String supabaseHost) {
        if (supabaseHost == null || supabaseHost.trim().isEmpty()) {
            return false;
        }
        return isLocalJdbcUrl(normalizedDirectUrl);
    }

    private static boolean isLocalJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return false;
        }
        String lower = jdbcUrl.toLowerCase();
        return lower.contains("//localhost:")
            || lower.contains("//127.0.0.1:")
            || lower.contains("//0.0.0.0:");
    }

    private static String getConfig(String key) {
        String env = System.getenv(key);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String fileValue = FILE_CONFIG.get(key);
        return fileValue != null && !fileValue.trim().isEmpty() ? fileValue.trim() : null;
    }

    private static Map<String, String> loadEnvFileConfig() {
        Map<String, String> values = new HashMap<>();
        Path[] candidates = new Path[] {
            Paths.get(".env"),
            Paths.get(".env.example"),
            Paths.get("backend", ".env"),
            Paths.get("backend", ".env.example")
        };

        for (Path candidate : candidates) {
            if (!Files.exists(candidate)) {
                continue;
            }
            try {
                List<String> lines = Files.readAllLines(candidate);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    int idx = trimmed.indexOf('=');
                    if (idx <= 0) {
                        continue;
                    }
                    String key = trimmed.substring(0, idx).trim();
                    String value = trimmed.substring(idx + 1).trim();
                    if (!key.isEmpty() && !values.containsKey(key)) {
                        values.put(key, value);
                    }
                }
                break;
            } catch (Exception ignored) {
                // If a candidate file cannot be read, try the next one.
            }
        }

        return values;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) return first.trim();
        if (second != null && !second.trim().isEmpty()) return second.trim();
        return null;
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        String value = firstNonBlank(first, second);
        return value != null ? value : fallback;
    }

    private static void initializeIfNeeded(Connection conn) throws SQLException {
        if (initialized) return;

        synchronized (DBConnection.class) {
            if (initialized) return;

            // Idempotent schema creation for first run on a fresh database.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," +
                    "fullName VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "phone VARCHAR(20)," +
                    "address VARCHAR(255))");

                stmt.execute("CREATE TABLE IF NOT EXISTS plants (" +
                    "id SERIAL PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "description TEXT," +
                    "price DECIMAL(10,2) NOT NULL," +
                    "imageUrl VARCHAR(255))");

                stmt.execute("CREATE TABLE IF NOT EXISTS cart_items (" +
                    "id SERIAL PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "plantId INT NOT NULL," +
                    "quantity INT NOT NULL DEFAULT 1," +
                    "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (plantId) REFERENCES plants(id) ON DELETE CASCADE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id SERIAL PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "totalPrice DECIMAL(10,2) NOT NULL," +
                    "orderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE)");

                stmt.execute("ALTER TABLE orders ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING'");

                stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id SERIAL PRIMARY KEY," +
                    "orderId INT NOT NULL," +
                    "plantId INT NOT NULL," +
                    "quantity INT NOT NULL," +
                    "price DECIMAL(10,2) NOT NULL," +
                    "FOREIGN KEY (orderId) REFERENCES orders(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (plantId) REFERENCES plants(id))");
            }

            seedPlantsIfEmpty(conn);
            initialized = true;
        }
    }

    private static void seedPlantsIfEmpty(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM plants";
        try (PreparedStatement countStmt = conn.prepareStatement(countSql);
             ResultSet rs = countStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertSql = "INSERT INTO plants (name, description, price, imageUrl) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            insertPlant(insert, "Aloe Vera", "A succulent plant great for skin care and decoration.", 20.00, "images/aloe.jpg");
            insertPlant(insert, "Rose", "A classic flowering plant available in many colours.", 15.00, "images/rose.jpg");
            insertPlant(insert, "Mint", "Aromatic herb perfect for teas and cooking.", 10.00, "images/mint.jpg");
            insertPlant(insert, "Peace Lily", "An elegant indoor plant that purifies the air.", 25.00, "images/lily.jpg");
            insertPlant(insert, "Snake Plant", "Very low maintenance - great for beginners.", 30.00, "images/snake.jpg");
            insertPlant(insert, "Bamboo Palm", "Tropical palm that thrives indoors with bright light.", 45.00, "images/bamboo.jpg");
        }
    }

    private static void insertPlant(PreparedStatement insert, String name, String description,
                                    double price, String imageUrl) throws SQLException {
        insert.setString(1, name);
        insert.setString(2, description);
        insert.setDouble(3, price);
        insert.setString(4, imageUrl);
        insert.executeUpdate();
    }
}
