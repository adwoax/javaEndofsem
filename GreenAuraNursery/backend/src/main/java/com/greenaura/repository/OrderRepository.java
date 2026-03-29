package com.greenaura.repository;

import com.greenaura.dao.OrderDAO;
import com.greenaura.model.CartItem;
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
}
