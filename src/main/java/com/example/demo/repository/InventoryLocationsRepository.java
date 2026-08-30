package com.example.demo.repository;

import com.example.demo.entity.InventoryLocations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryLocationsRepository extends JpaRepository<InventoryLocations, Integer> {

    Optional<InventoryLocations> findByLocationType(String locationType);

    Optional<InventoryLocations> findByShelterShelterId(Integer shelterId);
}