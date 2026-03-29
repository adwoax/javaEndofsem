package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
public class ConfirmationController {
    @GetMapping("/confirmation")
    public ApiResponse<ConfirmationResponse> confirmation(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        ConfirmationResponse response = new ConfirmationResponse();
        response.setMessage("Your order has been confirmed!");
        if (loggedInUser != null) {
            response.setLoggedInUser(loggedInUser);
        }
        return new ApiResponse<>("Success", response);
    }
}
