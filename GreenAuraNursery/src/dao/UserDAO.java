package dao;

import model.User;
import util.DBConnection;

import java.sql.*;

/**
 * ============================================================
 *  UserDAO.java — Data Access Object for Users
 * ============================================================
 *
 *  OOP CONCEPT: ABSTRACTION
 *  ------------------------
 *  DAO = Data Access Object.
 *  This class ABSTRACTS all SQL operations related to users.
 *  Servlets never write SQL directly — they just call methods here.
 *
 *  Example:
 *      Instead of writing SQL in RegisterServlet, we call:
 *          UserDAO dao = new UserDAO();
 *          dao.registerUser(user);
 *
 *  This makes the code cleaner, reusable, and easier to fix.
 *  If the database changes, you only update the DAO — not every servlet.
 *
 *  OOP CONCEPT: SEPARATION OF CONCERNS
 *  ------------------------------------
 *  Each class has ONE job:
 *      - Model  → stores data (User.java)
 *      - DAO    → handles database (UserDAO.java)
 *      - Servlet → handles web requests (RegisterServlet.java)
 * ============================================================
 */
public class UserDAO {

    /**
     * Inserts a new user into the `users` table.
     * Called by RegisterServlet when a new customer signs up.
     *
     * @param user A User object with all signup details filled in
     * @return true if registration succeeded, false if it failed
     */
    public boolean registerUser(User user) {
        // SQL query with ? placeholders (prevents SQL injection)
        String sql = "INSERT INTO users (fullName, email, password, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?)";

        // Try-with-resources: automatically closes connection when done
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Fill in the ? placeholders with real values
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());

            // Execute the INSERT and check if 1 row was affected
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted == 1; // true = success

        } catch (SQLException e) {
            System.out.println("ERROR in registerUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Looks up a user by email and password.
     * Called by LoginServlet to validate login credentials.
     *
     * @param email    The email the user typed
     * @param password The password the user typed
     * @return A User object if found, or null if credentials are wrong
     */
    public User getUserByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // A matching user was found — build and return a User object
                return new User(
                    rs.getInt("id"),
                    rs.getString("fullName"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("address")
                );
            }

        } catch (SQLException e) {
            System.out.println("ERROR in getUserByEmailAndPassword: " + e.getMessage());
        }

        return null; // No match found
    }

    /**
     * Checks if an email is already registered.
     * Used during signup to prevent duplicate accounts.
     *
     * @param email The email to check
     * @return true if the email already exists in the database
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if at least one row found

        } catch (SQLException e) {
            System.out.println("ERROR in emailExists: " + e.getMessage());
        }

        return false;
    }
}
