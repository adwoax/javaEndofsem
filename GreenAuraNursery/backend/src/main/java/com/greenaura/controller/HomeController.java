package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.greenaura.service.PlantService;

@RestController
@RequestMapping("/api")
public class HomeController {
    private final PlantService plantService;

    public HomeController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeResponse> home(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        HomeResponse response = new HomeResponse();
        response.setBestSellers(plantService.getBestSellers(3));
        if (loggedInUser != null) {
            response.setLoggedInUser(loggedInUser);
        }
        return new ApiResponse<>("Success", response);
    }
}
