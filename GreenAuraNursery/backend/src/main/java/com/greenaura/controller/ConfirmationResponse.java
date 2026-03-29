package com.greenaura.controller;

import com.greenaura.model.User;

public class ConfirmationResponse {
    private User loggedInUser;
    private String message;

    public ConfirmationResponse() {
    }

    public ConfirmationResponse(User loggedInUser, String message) {
        this.loggedInUser = loggedInUser;
        this.message = message;
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
}
