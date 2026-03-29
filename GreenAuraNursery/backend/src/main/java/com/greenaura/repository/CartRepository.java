package com.greenaura.repository;

import com.greenaura.dao.CartDAO;
import com.greenaura.model.CartItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartRepository {
    private final CartDAO cartDAO;

    public CartRepository(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    public void addToCart(int userId, int plantId) {
        cartDAO.addToCart(userId, plantId);
    }

    public List<CartItem> findCartItemsByUserId(int userId) {
        return cartDAO.getCartItems(userId);
    }

    public double getCartTotalByUserId(int userId) {
        return cartDAO.getCartTotal(userId);
    }

    public void clearCartByUserId(int userId) {
        cartDAO.clearCart(userId);
    }
}
