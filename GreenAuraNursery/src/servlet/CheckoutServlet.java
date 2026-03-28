package servlet;

import dao.CartDAO;
import dao.OrderDAO;
import model.CartItem;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * ============================================================
 *  CheckoutServlet.java — Handles Checkout and Order Placement
 * ============================================================
 *
 *  OOP CONCEPT: INHERITANCE
 *  -------------------------
 *  extends HttpServlet — same pattern used across all servlets.
 *  This consistency is the power of Inheritance: all servlets
 *  share the same structure because they all inherit from HttpServlet.
 *
 *  THIS SERVLET HANDLES TWO THINGS:
 *
 *  1. doGet()  → Shows the checkout page with the user's details
 *                pre-filled from the session/database
 *
 *  2. doPost() → Processes "Place Order":
 *                - Saves order to database (via OrderDAO)
 *                - Clears the cart (via CartDAO)
 *                - Redirects to confirmation page
 *
 *  OOP CONCEPT: WORKING WITH MULTIPLE OBJECTS
 *  -------------------------------------------
 *  This servlet uses:
 *      - User object (from session)
 *      - List<CartItem> objects (from CartDAO)
 *      - CartDAO object (to fetch/clear cart)
 *      - OrderDAO object (to save the order)
 *
 *  URL MAPPING: /checkout
 * ============================================================
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private CartDAO  cartDAO  = new CartDAO();
    private OrderDAO orderDAO = new OrderDAO();

    /**
     * doGet() — Shows the checkout page
     * Pre-fills name, phone, address from the session user object
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------------------------------------
        // STEP 1: Make sure user is logged in
        // -------------------------------------------------------
        User loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // -------------------------------------------------------
        // STEP 2: Make sure their cart is not empty
        // -------------------------------------------------------
        List<CartItem> cartItems = cartDAO.getCartItems(loggedInUser.getId());
        if (cartItems.isEmpty()) {
            // Cart is empty — no point checking out
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // -------------------------------------------------------
        // STEP 3: Calculate the total
        // -------------------------------------------------------
        double total = cartDAO.getCartTotal(loggedInUser.getId());

        // -------------------------------------------------------
        // STEP 4: Pass data to the checkout page
        // The JSP page will display the user's name, phone, address
        // and the total amount to pay
        // -------------------------------------------------------
        request.setAttribute("cartItems",   cartItems);
        request.setAttribute("total",       total);
        request.setAttribute("loggedInUser", loggedInUser);

        request.getRequestDispatcher("/views/checkout.jsp").forward(request, response);
    }

    /**
     * doPost() — Processes the "Place Order" button click
     * Saves the order to the database and clears the cart
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------------------------------------
        // STEP 1: Verify user is still logged in
        // -------------------------------------------------------
        User loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = loggedInUser.getId();

        // -------------------------------------------------------
        // STEP 2: Fetch the current cart items and total
        // -------------------------------------------------------
        List<CartItem> cartItems = cartDAO.getCartItems(userId);
        double total = cartDAO.getCartTotal(userId);

        if (cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/catalogue");
            return;
        }

        // -------------------------------------------------------
        // STEP 3: Place the order using OrderDAO
        // This inserts into `orders` and `order_items` tables
        // Uses a database transaction (see OrderDAO.java)
        // -------------------------------------------------------
        boolean orderPlaced = orderDAO.placeOrder(userId, cartItems, total);

        if (orderPlaced) {
            // -------------------------------------------------------
            // STEP 4: Clear the cart now that order is placed
            // -------------------------------------------------------
            cartDAO.clearCart(userId);

            // -------------------------------------------------------
            // STEP 5: Redirect to confirmation page
            // -------------------------------------------------------
            response.sendRedirect(request.getContextPath() + "/confirmation");

        } else {
            // Order failed — send error back to checkout page
            request.setAttribute("errorMessage", "Failed to place order. Please try again.");
            request.setAttribute("loggedInUser", loggedInUser);
            request.getRequestDispatcher("/views/checkout.jsp").forward(request, response);
        }
    }

    /**
     * ============================================================
     *  HELPER METHOD: getLoggedInUser()
     * ============================================================
     *
     *  OOP CONCEPT: ABSTRACTION + REUSABLE METHODS
     *  ---------------------------------------------
     *  Instead of repeating the session-checking code in both
     *  doGet() and doPost(), we extract it into one private method.
     *
     *  This is a key programming principle: DRY (Don't Repeat Yourself).
     *  If we need to change how we check sessions, we change it in ONE place.
     *
     *  Private means only THIS class can use it — good encapsulation.
     *
     * @return The logged-in User object, or null if not logged in
     */
    private User getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (User) session.getAttribute("loggedInUser");
    }
}
