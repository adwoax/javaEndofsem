package com.greenaura.controller;

public class LogoutResponse {
    private String message;
    private String redirectUrl;

    public LogoutResponse() {
    }

    public LogoutResponse(String message, String redirectUrl) {
        this.message = message;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
