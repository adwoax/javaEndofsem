package com.greenaura.dao;

import com.greenaura.model.CartItem;
import com.greenaura.model.Plant;
import com.greenaura.util.DBConnection;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  CartDAO.java — Data Access Object for Cart Items
 * ============================================================
 *
 *  OOP CONCEPT: ABSTRACTION
 *  ------------------------
 *  All cart-related SQL lives here. Servlets don't touch SQL.
 *
 *  This DAO handles:
 *    - Adding a plant to the cart
 *    - Retrieving all items in a user's cart
 *    - Clearing the cart after checkout
 * ============================================================
 */
@Component
public class CartDAO {

    /**
     * Adds a plant to the user's cart.
     * If the plant is already in the cart, increases quantity by 1.
     * If it's new, inserts a fresh row.
     *
     * @param userId  The logged-in user's ID
     * @param plantId The plant they are adding
     */
    public void addToCart(int userId, int plantId) {
        // First, check if this plant is already in the user's cart
        String checkSql = "SELECT id, quantity FROM cart_items WHERE userId = ? AND plantId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, plantId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Plant already exists in cart → increase quantity by 1
                int currentQty = rs.getInt("quantity");
                int cartItemId = rs.getInt("id");

                String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, currentQty + 1);
                updateStmt.setInt(2, cartItemId);
                updateStmt.executeUpdate();

            } else {
                // Plant is new to the cart → insert a fresh row with quantity 1
                String insertSql = "INSERT INTO cart_items (userId, plantId, quantity) VALUES (?, ?, 1)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, plantId);
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("ERROR in addToCart: " + e.getMessage());
        }
    }

    /**
     * Removes one quantity of a plant from the user's cart.
     * If quantity reaches zero, the row is deleted.
     */
    public void removeFromCart(int userId, int plantId) {
        String checkSql = "SELECT id, quantity FROM cart_items WHERE userId = ? AND plantId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, plantId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                return;
            }

            int cartItemId = rs.getInt("id");
            int currentQty = rs.getInt("quantity");

            if (currentQty > 1) {
                String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, currentQty - 1);
                    updateStmt.setInt(2, cartItemId);
                    updateStmt.executeUpdate();
                }
            } else {
                String deleteSql = "DELETE FROM cart_items WHERE id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, cartItemId);
                    deleteStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR in removeFromCart: " + e.getMessage());
        }
    }

    /**
     * Retrieves all CartItem objects for a given user.
     * Used on the Cart page to display what's in the user's cart.
     *
     * Uses a JOIN to get plant details alongside cart quantities.
     *
     * @param userId The logged-in user's ID
     * @return A list of CartItem objects (each containing a Plant + quantity)
     */
    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();

        // SQL JOIN: combines cart_items + plants table in one query
        String sql = "SELECT ci.id, ci.userId, ci.quantity, " +
                     "p.id AS plantId, p.name, p.description, p.price, p.imageUrl " +
                     "FROM cart_items ci " +
                     "JOIN plants p ON ci.plantId = p.id " +
                     "WHERE ci.userId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Build a Plant object from the JOIN results
                Plant plant = new Plant(
                    rs.getInt("plantId"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("imageUrl")
                );

                // Build a CartItem that contains the Plant (Composition!)
                CartItem item = new CartItem(
                    rs.getInt("id"),
                    rs.getInt("userId"),
                    plant,         // Plant object embedded in CartItem
                    rs.getInt("quantity")
                );

                items.add(item);
            }

        } catch (SQLException e) {
            System.out.println("ERROR in getCartItems: " + e.getMessage());
        }

        return items;
    }

    /**
     * Calculates and returns the total price of all items in a user's cart.
     * Used on the Cart and Checkout pages.
     *
     * @param userId The logged-in user's ID
     * @return The total in GH₵ as a double
     */
    public double getCartTotal(int userId) {
        List<CartItem> items = getCartItems(userId); // Reuse existing method
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal(); // Uses CartItem's own computed method
        }
        return total;
    }

    /**
     * Deletes all cart items for a user.
     * Called after a successful checkout to empty the cart.
     *
     * @param userId The logged-in user's ID
     */
    public void clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE userId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("ERROR in clearCart: " + e.getMessage());
        }
    }
}
