package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.greenaura.service.CartService;
import com.greenaura.service.CheckoutService;

@RestController
@RequestMapping("/api")
public class CheckoutController {
    private final CartService cartService;
    private final CheckoutService checkoutService;

    public CheckoutController(CartService cartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkoutPage(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new CheckoutResponse());
        }

        int userId = loggedInUser.getId();
        if (cartService.getCartItems(userId).isEmpty()) {
            CheckoutResponse response = new CheckoutResponse();
            response.setMessage("Cart is empty");
            return new ApiResponse<>("error", "Cart is empty", response);
        }

        CheckoutResponse response = new CheckoutResponse(
            cartService.getCartItems(userId),
            cartService.getCartTotal(userId),
            loggedInUser
        );
        return new ApiResponse<>("Success", response);
    }

    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> placeOrder(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new CheckoutResponse());
        }

        int userId = loggedInUser.getId();
        if (cartService.getCartItems(userId).isEmpty()) {
            return new ApiResponse<>("error", "Cart is empty", new CheckoutResponse());
        }

        boolean orderPlaced = checkoutService.placeOrderForUser(userId);
        if (orderPlaced) {
            CheckoutResponse response = new CheckoutResponse();
            response.setOrderPlaced(true);
            response.setMessage("Order placed successfully!");
            response.setRedirectUrl("/api/confirmation");
            return new ApiResponse<>("success", "Order placed", response);
        }

        CheckoutResponse response = new CheckoutResponse(
            cartService.getCartItems(userId),
            cartService.getCartTotal(userId),
            loggedInUser
        );
        response.setMessage("Failed to place order. Please try again.");
        return new ApiResponse<>("error", "Order placement failed", response);
    }
}
