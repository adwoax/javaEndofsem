package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.greenaura.service.CartService;

@RestController
@RequestMapping("/api")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public ApiResponse<CartResponse> cart(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new CartResponse());
        }

        int userId = loggedInUser.getId();
        CartResponse response = new CartResponse(
            cartService.getCartItems(userId),
            cartService.getCartTotal(userId),
            loggedInUser
        );
        return new ApiResponse<>("Success", response);
    }
}
