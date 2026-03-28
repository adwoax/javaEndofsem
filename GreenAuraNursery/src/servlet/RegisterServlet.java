package servlet;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * ============================================================
 *  RegisterServlet.java — Handles User Sign Up
 * ============================================================
 *
 *  OOP CONCEPT: INHERITANCE ⭐
 *  ---------------------------
 *  This class EXTENDS HttpServlet.
 *  That means RegisterServlet IS-A HttpServlet.
 *
 *  HttpServlet (from Java EE) already knows how to:
 *    - Listen for HTTP requests
 *    - Parse GET vs POST requests
 *    - Send HTTP responses
 *
 *  By extending it, we INHERIT all that behaviour for free.
 *  We only need to OVERRIDE the methods we care about:
 *      doGet()  → what to do when browser visits the page
 *      doPost() → what to do when form is submitted
 *
 *  OOP CONCEPT: POLYMORPHISM
 *  --------------------------
 *  doGet() and doPost() are methods defined in HttpServlet.
 *  We OVERRIDE them with our own version.
 *  Java will call OUR version because of Polymorphism —
 *  the right method is chosen at runtime based on the actual object.
 *
 *  URL MAPPING:
 *  This servlet handles requests to: /register
 * ============================================================
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    // Create a UserDAO object to handle all database operations
    // OOP: we use an object (dao) to call methods on it
    private UserDAO userDAO = new UserDAO();

    /**
     * doGet() — Called when the browser VISITS /register
     * Just shows the signup form (signup.jsp)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Forward the browser to the signup page
        request.getRequestDispatcher("/views/signup.jsp").forward(request, response);
    }

    /**
     * doPost() — Called when the user SUBMITS the signup form
     * Reads the form data, validates it, saves to database.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------------------------------------
        // STEP 1: Read the form data sent by the user
        // request.getParameter("fieldName") matches the HTML input name=""
        // -------------------------------------------------------
        String fullName = request.getParameter("fullName");
        String email    = request.getParameter("email");
        String password = request.getParameter("password");
        String phone    = request.getParameter("phone");
        String address  = request.getParameter("address");

        // -------------------------------------------------------
        // STEP 2: Basic Validation
        // Make sure no fields are empty
        // -------------------------------------------------------
        if (fullName == null || fullName.trim().isEmpty() ||
            email    == null || email.trim().isEmpty()    ||
            password == null || password.trim().isEmpty()) {

            // Send error message back to the signup page
            request.setAttribute("errorMessage", "Please fill in all required fields.");
            request.getRequestDispatcher("/views/signup.jsp").forward(request, response);
            return; // Stop here — don't proceed further
        }

        // -------------------------------------------------------
        // STEP 3: Check if the email is already registered
        // -------------------------------------------------------
        if (userDAO.emailExists(email)) {
            request.setAttribute("errorMessage", "This email is already registered. Please login.");
            request.getRequestDispatcher("/views/signup.jsp").forward(request, response);
            return;
        }

        // -------------------------------------------------------
        // STEP 4: Create a User object (OOP in action!)
        // We use the User class as a blueprint and create an object
        // -------------------------------------------------------
        User newUser = new User();          // Create an empty User object
        newUser.setFullName(fullName);      // Fill it using setters (Encapsulation)
        newUser.setEmail(email);
        newUser.setPassword(password);      // Note: In production, hash this password!
        newUser.setPhone(phone);
        newUser.setAddress(address);

        // -------------------------------------------------------
        // STEP 5: Save the user to the database via UserDAO
        // The servlet doesn't write SQL — it delegates to the DAO
        // -------------------------------------------------------
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            // Registration worked! Send them to the login page with a success message
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } else {
            // Something went wrong with the database
            request.setAttribute("errorMessage", "Registration failed. Please try again.");
            request.getRequestDispatcher("/views/signup.jsp").forward(request, response);
        }
    }
}
