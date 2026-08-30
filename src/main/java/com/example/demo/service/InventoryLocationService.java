package com.example.demo.service;

import com.example.demo.entity.InventoryLocations;
import com.example.demo.repository.InventoryLocationsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryLocationService {

    private final InventoryLocationsRepository inventoryLocationsRepository;

    public InventoryLocationService(InventoryLocationsRepository inventoryLocationsRepository) {
        this.inventoryLocationsRepository = inventoryLocationsRepository;
    }

    public List<InventoryLocations> getAllLocations() {
        return inventoryLocationsRepository.findAll();
    }

    public Optional<InventoryLocations> getLocationById(Integer locationId) {
        return inventoryLocationsRepository.findById(locationId);
    }

    public Optional<InventoryLocations> findCentralLocation() {
        return inventoryLocationsRepository.findByLocationType("Central");
    }

    public InventoryLocations createLocation(InventoryLocations location) {
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Location name must not be empty");
        }
        if (location.getLocationType() == null || location.getLocationType().trim().isEmpty()) {
            throw new IllegalArgumentException("Location type must not be empty");
        }
        return inventoryLocationsRepository.save(location);
    }
}