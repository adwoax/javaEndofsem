package util;

// Import the JDBC classes we need to connect to MySQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ============================================================
 *  DBConnection.java — Database Connection Utility
 * ============================================================
 *
 *  OOP CONCEPT: ABSTRACTION
 *  ------------------------
 *  This class HIDES the complexity of setting up a database
 *  connection. Every other class that needs a connection simply
 *  calls:
 *       Connection conn = DBConnection.getConnection();
 *
 *  They don't need to know HOW it connects — just that it does.
 *  That is Abstraction: hiding implementation details and
 *  exposing only what is necessary.
 *
 *  OOP CONCEPT: STATIC METHOD (Class-level behaviour)
 *  ---------------------------------------------------
 *  getConnection() is static, meaning you call it on the CLASS
 *  itself, not on an object. This makes it easy to reuse
 *  without creating a DBConnection object every time.
 * ============================================================
 */
public class DBConnection {

    // -------------------------------------------------------
    // DATABASE SETTINGS — change these to match your MySQL setup
    // -------------------------------------------------------
    private static final String URL      = "jdbc:mysql://localhost:3306/green_aura_nursery";
    private static final String USERNAME = "root";       // your MySQL username
    private static final String PASSWORD = "";           // your MySQL password

    /**
     * Returns a live Connection to the MySQL database.
     *
     * How it works:
     *  1. Loads the MySQL JDBC driver (tells Java HOW to talk to MySQL)
     *  2. Creates and returns a Connection object using the URL, user, pass
     *
     * If anything goes wrong, it prints the error and returns null.
     */
    public static Connection getConnection() {
        try {
            // Step 1: Load the MySQL driver class into memory
            // This is required so Java knows it's working with MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Create the connection and return it
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (ClassNotFoundException e) {
            // This happens if the MySQL JDBC .jar is not in your project
            System.out.println("ERROR: MySQL JDBC Driver not found.");
            System.out.println("Fix: Add mysql-connector-java.jar to your project's lib folder.");
            e.printStackTrace();
        } catch (SQLException e) {
            // This happens if credentials are wrong or MySQL isn't running
            System.out.println("ERROR: Could not connect to the database.");
            System.out.println("Fix: Check your USERNAME, PASSWORD, and that MySQL is running.");
            e.printStackTrace();
        }
        return null; // Return null if connection failed
    }
}
