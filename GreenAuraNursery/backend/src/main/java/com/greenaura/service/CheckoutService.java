package com.greenaura.service;

import com.greenaura.model.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckoutService {
    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutService(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    public boolean placeOrderForUser(int userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            return false;
        }

        double total = cartService.getCartTotal(userId);
        boolean orderPlaced = orderService.placeOrder(userId, cartItems, total);
        if (orderPlaced) {
            cartService.clearCart(userId);
        }
        return orderPlaced;
    }
}
