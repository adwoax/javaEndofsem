package com.greenaura.repository;

import com.greenaura.dao.OrderDAO;
import com.greenaura.model.CartItem;
import com.greenaura.model.OrderHistoryItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepository {
    private final OrderDAO orderDAO;

    public OrderRepository(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    public boolean placeOrder(int userId, List<CartItem> cartItems, double total) {
        return orderDAO.placeOrder(userId, cartItems, total);
    }

    public List<OrderHistoryItem> findOrdersByUserId(int userId) {
        return orderDAO.findOrdersByUserId(userId);
    }

    public boolean cancelOrderIfAllowed(int userId, int orderId) {
        return orderDAO.cancelOrderIfAllowed(userId, orderId);
    }

    public List<OrderHistoryItem> findAllOrders() {
        return orderDAO.findAllOrders();
    }

    public boolean markOrderReceived(int orderId) {
        return orderDAO.markOrderReceived(orderId);
    }
}
