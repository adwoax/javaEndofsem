package com.greenaura.dao;

import com.greenaura.model.CartItem;
import com.greenaura.model.OrderHistoryItem;
import com.greenaura.util.DBConnection;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
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

    private static final int CANCELLATION_WINDOW_MINUTES = 30;

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
        String orderSql     = "INSERT INTO orders (userId, totalPrice, status) VALUES (?, ?, 'PENDING')";
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

    public List<OrderHistoryItem> findOrdersByUserId(int userId) {
        String sql = "SELECT o.id, o.totalPrice, o.orderDate, COALESCE(o.status, 'PENDING') AS status, " +
            "COALESCE(SUM(oi.quantity), 0) AS itemCount " +
            "FROM orders o " +
            "LEFT JOIN order_items oi ON oi.orderId = o.id " +
            "WHERE o.userId = ? " +
            "GROUP BY o.id, o.totalPrice, o.orderDate, o.status " +
            "ORDER BY o.orderDate DESC";

        List<OrderHistoryItem> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp orderDate = rs.getTimestamp("orderDate");
                String status = rs.getString("status");
                boolean canCancel = isCancellableStatus(status) && withinCancellationWindow(orderDate);

                OrderHistoryItem item = new OrderHistoryItem(
                    rs.getInt("id"),
                    rs.getDouble("totalPrice"),
                    orderDate,
                    status,
                    rs.getInt("itemCount"),
                    canCancel
                );
                orders.add(item);
            }

        } catch (SQLException e) {
            System.out.println("ERROR in findOrdersByUserId: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public List<OrderHistoryItem> findAllOrders() {
        String sql = "SELECT o.id, o.totalPrice, o.orderDate, COALESCE(o.status, 'PENDING') AS status, " +
            "COALESCE(SUM(oi.quantity), 0) AS itemCount " +
            "FROM orders o " +
            "LEFT JOIN order_items oi ON oi.orderId = o.id " +
            "GROUP BY o.id, o.totalPrice, o.orderDate, o.status " +
            "ORDER BY o.orderDate DESC";

        List<OrderHistoryItem> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp orderDate = rs.getTimestamp("orderDate");
                String status = rs.getString("status");
                boolean canCancel = isCancellableStatus(status) && withinCancellationWindow(orderDate);

                OrderHistoryItem item = new OrderHistoryItem(
                    rs.getInt("id"),
                    rs.getDouble("totalPrice"),
                    orderDate,
                    status,
                    rs.getInt("itemCount"),
                    canCancel
                );
                orders.add(item);
            }

        } catch (SQLException e) {
            System.out.println("ERROR in findAllOrders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public boolean markOrderReceived(int orderId) {
        String sql = "UPDATE orders " +
            "SET status = 'RECEIVED' " +
            "WHERE id = ? " +
            "AND COALESCE(status, 'PENDING') = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERROR in markOrderReceived: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelOrderIfAllowed(int userId, int orderId) {
        String sql = "UPDATE orders " +
            "SET status = 'CANCELLED' " +
            "WHERE id = ? " +
            "AND userId = ? " +
            "AND COALESCE(status, 'PENDING') = 'PENDING' " +
            "AND orderDate >= (CURRENT_TIMESTAMP - INTERVAL '" + CANCELLATION_WINDOW_MINUTES + " minutes')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERROR in cancelOrderIfAllowed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isCancellableStatus(String status) {
        return "PENDING".equalsIgnoreCase(String.valueOf(status));
    }

    private boolean withinCancellationWindow(Timestamp orderDate) {
        if (orderDate == null) {
            return false;
        }
        long orderTime = orderDate.getTime();
        long cutoffMillis = CANCELLATION_WINDOW_MINUTES * 60L * 1000L;
        long age = System.currentTimeMillis() - orderTime;
        return age >= 0 && age <= cutoffMillis;
    }
}
