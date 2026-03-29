package com.greenaura.model;

import java.util.Date;

public class OrderHistoryItem {
    private int id;
    private double totalPrice;
    private Date orderDate;
    private String status;
    private int itemCount;
    private boolean cancellable;

    public OrderHistoryItem() {
    }

    public OrderHistoryItem(int id, double totalPrice, Date orderDate, String status, int itemCount, boolean cancellable) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.itemCount = itemCount;
        this.cancellable = cancellable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }
}
