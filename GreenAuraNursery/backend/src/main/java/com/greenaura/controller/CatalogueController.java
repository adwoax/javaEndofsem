package com.greenaura.controller;

import jakarta.servlet.http.HttpSession;
import com.greenaura.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.greenaura.service.PlantService;

@RestController
@RequestMapping("/api")
public class CatalogueController {
    private final PlantService plantService;

    public CatalogueController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping("/catalogue")
    public ApiResponse<CatalogueResponse> catalogue(@RequestParam(value = "added", required = false) String added, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String successMessage = null;
        if ("true".equals(added)) {
            successMessage = "Plant added to your cart!";
        }

        CatalogueResponse response = new CatalogueResponse();
        response.setPlants(plantService.getAllPlants());
        response.setSuccessMessage(successMessage);
        if (loggedInUser != null) {
            response.setLoggedInUser(loggedInUser);
        }
        return new ApiResponse<>("Success", response);
    }
}
