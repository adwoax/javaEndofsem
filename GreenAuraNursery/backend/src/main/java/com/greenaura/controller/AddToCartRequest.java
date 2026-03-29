package com.greenaura.controller;

public class AddToCartRequest {
    private Integer plantId;

    public AddToCartRequest() {
    }

    public AddToCartRequest(Integer plantId) {
        this.plantId = plantId;
    }

    public Integer getPlantId() {
        return plantId;
    }

    public void setPlantId(Integer plantId) {
        this.plantId = plantId;
    }
}
