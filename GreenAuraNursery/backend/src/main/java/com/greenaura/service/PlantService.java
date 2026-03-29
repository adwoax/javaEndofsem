package com.greenaura.service;

import com.greenaura.model.Plant;
import org.springframework.stereotype.Service;
import com.greenaura.repository.PlantRepository;

import java.util.List;

@Service
public class PlantService {
    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    public List<Plant> getAllPlants() {
        return plantRepository.findAll();
    }

    public List<Plant> getBestSellers(int maxItems) {
        List<Plant> allPlants = plantRepository.findAll();
        if (allPlants.size() <= maxItems) {
            return allPlants;
        }
        return allPlants.subList(0, maxItems);
    }
}
