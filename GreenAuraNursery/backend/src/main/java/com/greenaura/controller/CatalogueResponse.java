package com.greenaura.controller;

import com.greenaura.model.Plant;
import com.greenaura.model.User;
import java.util.List;

public class CatalogueResponse {
    private List<Plant> plants;
    private User loggedInUser;
    private String successMessage;

    public CatalogueResponse() {
    }

    public CatalogueResponse(List<Plant> plants, User loggedInUser, String successMessage) {
        this.plants = plants;
        this.loggedInUser = loggedInUser;
        this.successMessage = successMessage;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
