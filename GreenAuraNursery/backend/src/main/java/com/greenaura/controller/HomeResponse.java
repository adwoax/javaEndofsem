package com.greenaura.controller;

import com.greenaura.model.Plant;
import com.greenaura.model.User;
import java.util.List;

public class HomeResponse {
    private List<Plant> bestSellers;
    private User loggedInUser;

    public HomeResponse() {
    }

    public HomeResponse(List<Plant> bestSellers, User loggedInUser) {
        this.bestSellers = bestSellers;
        this.loggedInUser = loggedInUser;
    }

    public List<Plant> getBestSellers() {
        return bestSellers;
    }

    public void setBestSellers(List<Plant> bestSellers) {
        this.bestSellers = bestSellers;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
