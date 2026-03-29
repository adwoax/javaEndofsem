package com.greenaura.service;

import com.greenaura.model.CartItem;
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
}
