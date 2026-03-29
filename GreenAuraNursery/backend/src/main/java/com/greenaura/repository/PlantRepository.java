package com.greenaura.repository;

import com.greenaura.dao.PlantDAO;
import com.greenaura.model.Plant;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlantRepository {
    private final PlantDAO plantDAO;

    public PlantRepository(PlantDAO plantDAO) {
        this.plantDAO = plantDAO;
    }

    public List<Plant> findAll() {
        return plantDAO.getAllPlants();
    }

    public Plant findById(int id) {
        return plantDAO.getPlantById(id);
    }
}
