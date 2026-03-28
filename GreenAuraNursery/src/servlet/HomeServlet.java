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
 * HomeServlet — Serves the landing page with best sellers
 * URL: /home  (also mapped as welcome page in web.xml)
 *
 * OOP: Extends HttpServlet (Inheritance)
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private PlantDAO plantDAO = new PlantDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Fetch first 3 plants to use as "Best Sellers"
        List<Plant> allPlants = plantDAO.getAllPlants();
        List<Plant> bestSellers = allPlants.size() >= 3
                ? allPlants.subList(0, 3)
                : allPlants;

        // Check session for logged-in user
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("loggedInUser");
            request.setAttribute("loggedInUser", user);
        }

        request.setAttribute("bestSellers", bestSellers);
        request.getRequestDispatcher("/views/home.jsp").forward(request, response);
    }
}
