package servlet;

import dao.CartDAO;
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
 * CartServlet — Displays the user's cart
 *
 * OOP: Extends HttpServlet (Inheritance)
 * Works with List<CartItem> — each CartItem contains a Plant (Composition)
 * Checks session for logged-in user (Encapsulation via getter)
 *
 * URL: /cart
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private CartDAO cartDAO = new CartDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        User loggedInUser = null;

        if (session != null) {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Fetch this user's cart items
        List<CartItem> cartItems = cartDAO.getCartItems(loggedInUser.getId());
        double total = cartDAO.getCartTotal(loggedInUser.getId());

        // Pass to view
        request.setAttribute("cartItems",    cartItems);
        request.setAttribute("total",        total);
        request.setAttribute("loggedInUser", loggedInUser);

        request.getRequestDispatcher("/views/cart.jsp").forward(request, response);
    }
}
