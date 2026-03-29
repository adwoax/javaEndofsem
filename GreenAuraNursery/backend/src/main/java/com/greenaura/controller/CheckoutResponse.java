package com.greenaura.controller;

import com.greenaura.model.CartItem;
import com.greenaura.model.User;
import java.util.List;

public class CheckoutResponse {
    private List<CartItem> cartItems;
    private double total;
    private User loggedInUser;
    private String message;
    private boolean orderPlaced;
    private String redirectUrl;

    public CheckoutResponse() {
    }

    public CheckoutResponse(List<CartItem> cartItems, double total, User loggedInUser) {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }

    public void setOrderPlaced(boolean orderPlaced) {
        this.orderPlaced = orderPlaced;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
