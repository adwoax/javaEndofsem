package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.greenaura.service.CartService;

@RestController
@RequestMapping("/api")
public class AddToCartController {
    private final CartService cartService;

    public AddToCartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/addToCart")
    public ApiResponse<AddToCartResponse> addToCart(@RequestBody AddToCartRequest request, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            AddToCartResponse response = new AddToCartResponse("Not authenticated", false, "/api/register");
            return new ApiResponse<>("error", "Not authenticated", response);
        }

        Integer plantId = request.getPlantId();
        if (plantId == null) {
            AddToCartResponse response = new AddToCartResponse("Invalid plant ID", false, "/api/catalogue");
            return new ApiResponse<>("error", "Invalid plant ID", response);
        }

        cartService.addToCart(loggedInUser.getId(), plantId);
        AddToCartResponse response = new AddToCartResponse("Plant added to your cart!", true, "/api/catalogue?added=true");
        return new ApiResponse<>("success", "Plant added to cart", response);
    }
}
