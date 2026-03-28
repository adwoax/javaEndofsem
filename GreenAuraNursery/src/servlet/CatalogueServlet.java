package servlet;

import dao.PlantDAO;
import model.Plant;
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
 * CatalogueServlet — Displays all plants from the database
 *
 * OOP: Extends HttpServlet (Inheritance)
 * Uses PlantDAO to fetch plants (Abstraction)
 * Works with List<Plant> objects (Classes & Objects)
 *
 * URL: /catalogue
 */
@WebServlet("/catalogue")
public class CatalogueServlet extends HttpServlet {

    private PlantDAO plantDAO = new PlantDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Fetch all plants from the database as a List of Plant objects
        List<Plant> plants = plantDAO.getAllPlants();

        // Check if a plant was just added (to show success banner)
        String added = request.getParameter("added");
        if ("true".equals(added)) {
            request.setAttribute("successMessage", "Plant added to your cart!");
        }

        // Get logged-in user from session (may be null if not logged in)
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("loggedInUser");
            request.setAttribute("loggedInUser", user);
        }

        // Pass the plants list to the JSP view
        request.setAttribute("plants", plants);
        request.getRequestDispatcher("/views/catalogue.jsp").forward(request, response);
    }
}
