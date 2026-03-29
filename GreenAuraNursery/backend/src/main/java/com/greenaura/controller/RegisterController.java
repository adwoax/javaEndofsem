package com.greenaura.controller;

import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.greenaura.service.UserService;

@RestController
@RequestMapping("/api")
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public ApiResponse<RegisterResponse> registerPage() {
        RegisterResponse response = new RegisterResponse("Ready to register", false, null);
        return new ApiResponse<>("success", response);
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        String fullName = request.getFullName();
        String email = request.getEmail();
        String password = request.getPassword();
        String phone = request.getPhone();
        String address = request.getAddress();

        if (fullName == null || fullName.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            RegisterResponse response = new RegisterResponse("Please fill in all required fields.", false, null);
            return new ApiResponse<>("error", "Validation failed", response);
        }

        if (userService.emailExists(email)) {
            RegisterResponse response = new RegisterResponse("This email is already registered. Please login.", false, null);
            return new ApiResponse<>("error", "Email already exists", response);
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setPhone(phone);
        newUser.setAddress(address);

        boolean registered = userService.registerUser(newUser);
        if (!registered) {
            RegisterResponse response = new RegisterResponse("Registration failed. Please try again.", false, null);
            return new ApiResponse<>("error", "Registration failed", response);
        }

        RegisterResponse response = new RegisterResponse("Registration successful!", true, "/api/login?registered=true");
        return new ApiResponse<>("success", "Registration successful", response);
    }
}
