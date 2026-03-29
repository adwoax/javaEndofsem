package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.greenaura.service.UserService;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public ApiResponse<LoginResponse> loginPage() {
        LoginResponse response = new LoginResponse();
        response.setMessage("Please provide credentials");
        return new ApiResponse<>("success", response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            LoginResponse response = new LoginResponse();
            response.setMessage("Please enter your email and password.");
            return new ApiResponse<>("error", "Validation failed", response);
        }

        User user = userService.authenticate(email, password);
        if (user == null) {
            LoginResponse response = new LoginResponse();
            response.setMessage("Invalid email or password. Please try again.");
            return new ApiResponse<>("error", "Authentication failed", response);
        }

        session.setAttribute("loggedInUser", user);
        session.setMaxInactiveInterval(30 * 60);
        LoginResponse response = new LoginResponse(user, "Login successful");
        return new ApiResponse<>("success", "Login successful", response);
    }
}
