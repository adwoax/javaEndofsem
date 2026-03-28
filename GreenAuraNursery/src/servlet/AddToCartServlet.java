package servlet;

import dao.CartDAO;
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
 *  AddToCartServlet.java — Handles "Add to Cart" Button
 * ============================================================
 *
 *  OOP CONCEPT: INHERITANCE
 *  -------------------------
 *  extends HttpServlet — we inherit HTTP request/response handling.
 *  We override doPost() because "Add to Cart" is a form POST action.
 *
 *  KEY LOGIC:
 *  ----------
 *  Before adding to cart, we check if the user is logged in.
 *  We do this by checking the SESSION for a "loggedInUser" object.
 *
 *    - If logged in  → add to cart, redirect to catalogue
 *    - If NOT logged in → redirect to sign up page
 *
 *  This enforces the flow from the wireframe:
 *      [Add to Cart] → (if not logged in) → [Sign Up] → [Login]
 *
 *  URL MAPPING: /addToCart
 * ============================================================
 */
@WebServlet("/addToCart")
public class AddToCartServlet extends HttpServlet {

    private CartDAO cartDAO = new CartDAO();

    /**
     * doPost() — Called when user clicks "Add to Cart" on the catalogue
     * The catalogue form sends: plantId (hidden input)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------------------------------------
        // STEP 1: Check if the user is logged in
        // We look in the session for the User object we stored at login
        // -------------------------------------------------------
        HttpSession session = request.getSession(false); // false = don't create a new session
        User loggedInUser = null;

        if (session != null) {
            // Retrieve the User object from the session
            // We cast it because getAttribute() returns Object type
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }

        // -------------------------------------------------------
        // STEP 2: If NOT logged in → redirect to sign up
        // -------------------------------------------------------
        if (loggedInUser == null) {
            // User is not authenticated — send them to register first
            response.sendRedirect(request.getContextPath() + "/register");
            return; // Stop here
        }

        // -------------------------------------------------------
        // STEP 3: Get the plantId from the form submission
        // The "Add to Cart" button form has a hidden input: <input name="plantId" value="...">
        // -------------------------------------------------------
        String plantIdParam = request.getParameter("plantId");

        if (plantIdParam == null || plantIdParam.trim().isEmpty()) {
            // No plantId was sent — something is wrong
            response.sendRedirect(request.getContextPath() + "/catalogue");
            return;
        }

        // Convert the String plantId to an integer
        int plantId = Integer.parseInt(plantIdParam);

        // -------------------------------------------------------
        // STEP 4: Add the plant to the cart using CartDAO
        // We pass the logged-in user's ID + the selected plant's ID
        // -------------------------------------------------------
        int userId = loggedInUser.getId(); // Get user ID from User object (Encapsulation)
        cartDAO.addToCart(userId, plantId);

        // -------------------------------------------------------
        // STEP 5: Redirect back to the catalogue with a success message
        // -------------------------------------------------------
        response.sendRedirect(request.getContextPath() + "/catalogue?added=true");
    }
}
