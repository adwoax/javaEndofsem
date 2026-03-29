package com.greenaura.service;

import com.greenaura.model.CartItem;
import org.springframework.stereotype.Service;
import com.greenaura.repository.CartRepository;

import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void addToCart(int userId, int plantId) {
        cartRepository.addToCart(userId, plantId);
    }

    public void removeFromCart(int userId, int plantId) {
        cartRepository.removeFromCart(userId, plantId);
    }

    public List<CartItem> getCartItems(int userId) {
        return cartRepository.findCartItemsByUserId(userId);
    }

    public double getCartTotal(int userId) {
        return cartRepository.getCartTotalByUserId(userId);
    }

    public void clearCart(int userId) {
        cartRepository.clearCartByUserId(userId);
    }
}
