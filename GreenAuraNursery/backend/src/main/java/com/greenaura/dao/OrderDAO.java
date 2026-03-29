package com.greenaura.dao;

import com.greenaura.model.CartItem;
import com.greenaura.util.DBConnection;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;

/**
 * ============================================================
 *  OrderDAO.java — Data Access Object for Orders
 * ============================================================
 *
 *  OOP CONCEPT: ABSTRACTION
 *  ------------------------
 *  All order-related SQL is hidden here.
 *  CheckoutServlet just calls placeOrder() — it doesn't
 *  know or care how it works internally.
 *
 *  DATABASE CONCEPT: TRANSACTIONS
 *  --------------------------------
 *  Placing an order involves TWO steps:
 *    1. Insert into `orders` table
 *    2. Insert each item into `order_items` table
 *
 *  Both must succeed together. If step 2 fails, step 1 should
 *  also be undone. This is called a TRANSACTION.
 *  We use conn.setAutoCommit(false) to control this manually.
 * ============================================================
 */
@Component
public class OrderDAO {

    /**
     * Places an order by:
     *  1. Inserting a row into the `orders` table
     *  2. Inserting one row per item into `order_items`
     *
     * Uses a database TRANSACTION so both succeed or both fail.
     *
     * @param userId     The ID of the logged-in user
     * @param cartItems  The list of items in their cart
     * @param total      The total price of the order
     * @return true if the order was placed successfully
     */
    public boolean placeOrder(int userId, List<CartItem> cartItems, double total) {
        String orderSql     = "INSERT INTO orders (userId, totalPrice) VALUES (?, ?)";
        String orderItemSql = "INSERT INTO order_items (orderId, plantId, quantity, price) " +
                              "VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();

            // ---- START TRANSACTION ----
            // Turn off auto-commit so we control when changes are saved
            conn.setAutoCommit(false);

            // STEP 1: Insert the order and get the generated order ID
            PreparedStatement orderStmt = conn.prepareStatement(
                orderSql, Statement.RETURN_GENERATED_KEYS
            );
            orderStmt.setInt(1, userId);
            orderStmt.setDouble(2, total);
            orderStmt.executeUpdate();

            // Retrieve the auto-generated order ID
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            }

            // STEP 2: Insert each cart item as an order_item row
            PreparedStatement itemStmt = conn.prepareStatement(orderItemSql);

            for (CartItem item : cartItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getPlant().getId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPlant().getPrice());
                itemStmt.addBatch(); // Queue this row (more efficient than executing one by one)
            }

            itemStmt.executeBatch(); // Execute all queued rows at once

            // ---- COMMIT TRANSACTION ----
            // Everything succeeded — save the changes permanently
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("ERROR in placeOrder: " + e.getMessage());

            // ---- ROLLBACK TRANSACTION ----
            // Something failed — undo everything to keep data consistent
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;

        } finally {
            // Always restore auto-commit and close connection
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
