package com.greenaura.controller;

import com.greenaura.model.CartItem;
import com.greenaura.model.User;
import java.util.List;

public class CartResponse {
    private List<CartItem> cartItems;
    private double total;
    private User loggedInUser;

    public CartResponse() {
    }

    public CartResponse(List<CartItem> cartItems, double total, User loggedInUser) {
        this.cartItems = cartItems;
        this.total = total;
        this.loggedInUser = loggedInUser;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
