package dao;

import model.Plant;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  PlantDAO.java — Data Access Object for Plants
 * ============================================================
 *
 *  OOP CONCEPT: ABSTRACTION
 *  ------------------------
 *  All SQL related to plants lives here.
 *  CatalogueServlet just calls PlantDAO methods — it never
 *  writes SQL itself.
 *
 *  OOP CONCEPT: COLLECTIONS (List of Objects)
 *  -------------------------------------------
 *  getAllPlants() returns a List<Plant> — a collection of
 *  Plant objects. This is how Java handles "multiple results"
 *  from a database query.
 * ============================================================
 */
public class PlantDAO {

    /**
     * Retrieves ALL plants from the database for the catalogue page.
     *
     * @return A List of Plant objects (one per row in the `plants` table)
     */
    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>(); // Start with an empty list
        String sql = "SELECT * FROM plants";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through every row returned by the query
            while (rs.next()) {
                // Create a Plant object from each row and add to list
                Plant plant = new Plant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("imageUrl")
                );
                plants.add(plant);
            }

        } catch (SQLException e) {
            System.out.println("ERROR in getAllPlants: " + e.getMessage());
        }

        return plants; // Return the full list (may be empty if error occurred)
    }

    /**
     * Retrieves a single plant by its ID.
     * Used when adding a specific plant to the cart.
     *
     * @param plantId The ID of the plant to find
     * @return A Plant object, or null if not found
     */
    public Plant getPlantById(int plantId) {
        String sql = "SELECT * FROM plants WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, plantId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Plant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("imageUrl")
                );
            }

        } catch (SQLException e) {
            System.out.println("ERROR in getPlantById: " + e.getMessage());
        }

        return null;
    }
}
