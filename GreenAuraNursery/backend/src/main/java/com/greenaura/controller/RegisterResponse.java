package com.greenaura.controller;

public class RegisterResponse {
    private String message;
    private boolean success;
    private String redirectUrl;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, boolean success, String redirectUrl) {
        this.message = message;
        this.success = success;
        this.redirectUrl = redirectUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
