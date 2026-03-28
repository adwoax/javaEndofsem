package servlet;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * ============================================================
 *  LoginServlet.java — Handles User Login
 * ============================================================
 *
 *  OOP CONCEPT: INHERITANCE
 *  -------------------------
 *  extends HttpServlet — same pattern as RegisterServlet.
 *  LoginServlet IS-A HttpServlet and inherits its HTTP handling.
 *
 *  NEW CONCEPT: SESSION
 *  --------------------
 *  HTTP is stateless — each request is independent.
 *  A SESSION is how we "remember" a user across pages.
 *
 *  After login, we store the User object in the session:
 *      session.setAttribute("loggedInUser", user);
 *
 *  Then on any other page, we can retrieve it:
 *      User user = (User) session.getAttribute("loggedInUser");
 *
 *  If this is null → user is not logged in.
 *
 *  URL MAPPING: /login
 * ============================================================
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    /**
     * doGet() — Shows the login form
     * Also checks if user just registered (shows success message)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if they were redirected here after registration
        String registered = request.getParameter("registered");
        if ("true".equals(registered)) {
            request.setAttribute("successMessage", "Account created! Please log in.");
        }

        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    /**
     * doPost() — Processes the login form submission
     * Validates credentials, creates a session, redirects to catalogue
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------------------------------------
        // STEP 1: Read login form inputs
        // -------------------------------------------------------
        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        // -------------------------------------------------------
        // STEP 2: Validate — don't allow empty fields
        // -------------------------------------------------------
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            request.setAttribute("errorMessage", "Please enter your email and password.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        // -------------------------------------------------------
        // STEP 3: Check credentials against the database
        // Returns a User object if correct, or null if wrong
        // -------------------------------------------------------
        User user = userDAO.getUserByEmailAndPassword(email, password);

        if (user != null) {
            // -------------------------------------------------------
            // STEP 4: Login successful! Create a SESSION
            //
            // A session is like a "locker" on the server.
            // We store the User object there so we can access it
            // from any page without asking the user to log in again.
            // -------------------------------------------------------
            HttpSession session = request.getSession();
            session.setAttribute("loggedInUser", user); // Store User object in session
            session.setMaxInactiveInterval(30 * 60);    // Session expires after 30 minutes

            // Redirect to the catalogue (they can now shop)
            response.sendRedirect(request.getContextPath() + "/catalogue");

        } else {
            // -------------------------------------------------------
            // STEP 5: Login failed — send error back to login page
            // -------------------------------------------------------
            request.setAttribute("errorMessage", "Invalid email or password. Please try again.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }
}
