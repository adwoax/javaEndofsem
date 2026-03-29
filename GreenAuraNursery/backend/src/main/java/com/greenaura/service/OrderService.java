package com.greenaura.service;

import com.greenaura.model.CartItem;
import com.greenaura.model.OrderHistoryItem;
import org.springframework.stereotype.Service;
import com.greenaura.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public boolean placeOrder(int userId, List<CartItem> cartItems, double total) {
        return orderRepository.placeOrder(userId, cartItems, total);
    }

    public List<OrderHistoryItem> getOrdersByUserId(int userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    public boolean cancelOrderIfAllowed(int userId, int orderId) {
        return orderRepository.cancelOrderIfAllowed(userId, orderId);
    }

    public List<OrderHistoryItem> getAllOrders() {
        return orderRepository.findAllOrders();
    }

    public boolean markOrderReceived(int orderId) {
        return orderRepository.markOrderReceived(orderId);
    }
}
