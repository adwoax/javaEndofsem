package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
public class LogoutController {
    @GetMapping("/logout")
    public ApiResponse<LogoutResponse> logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        LogoutResponse response = new LogoutResponse("Logged out successfully", "/api/home");
        return new ApiResponse<>("success", "Logout successful", response);
    }
}
