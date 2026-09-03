package com.example.demo.controller;

import com.example.demo.entity.InventoryLocations;
import com.example.demo.service.InventoryLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-locations")
public class InventoryLocationController {

    private final InventoryLocationService inventoryLocationService;

    public InventoryLocationController(InventoryLocationService inventoryLocationService) {
        this.inventoryLocationService = inventoryLocationService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryLocations>> getLocations() {
        return ResponseEntity.ok(inventoryLocationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryLocations> getLocationById(@PathVariable Integer id) {
        return inventoryLocationService.getLocationById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createLocation(@RequestBody InventoryLocations location) {
        try {
            InventoryLocations created = inventoryLocationService.createLocation(location);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}