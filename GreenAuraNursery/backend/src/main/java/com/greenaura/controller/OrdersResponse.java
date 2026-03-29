package com.greenaura.controller;

import com.greenaura.model.OrderHistoryItem;

import java.util.ArrayList;
import java.util.List;

public class OrdersResponse {
    private List<OrderHistoryItem> orders = new ArrayList<>();
    private int totalOrders;
    private int receivedOrders;
    private int pendingOrders;
    private int cancelledOrders;

    public OrdersResponse() {
    }

    public OrdersResponse(List<OrderHistoryItem> orders, int totalOrders, int receivedOrders, int pendingOrders, int cancelledOrders) {
        this.orders = orders;
        this.totalOrders = totalOrders;
        this.receivedOrders = receivedOrders;
        this.pendingOrders = pendingOrders;
        this.cancelledOrders = cancelledOrders;
    }

    public List<OrderHistoryItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderHistoryItem> orders) {
        this.orders = orders;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getReceivedOrders() {
        return receivedOrders;
    }

    public void setReceivedOrders(int receivedOrders) {
        this.receivedOrders = receivedOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public int getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(int cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
}
