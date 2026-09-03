package com.example.demo.controller;

import com.example.demo.entity.InventoryStock;
import com.example.demo.service.InventoryStockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-stock")
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    public InventoryStockController(InventoryStockService inventoryStockService) {
        this.inventoryStockService = inventoryStockService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryStock>> getStock(@RequestParam(required = false) Integer locationId,
                                                         @RequestParam(required = false) Integer threshold) {
        if (locationId != null && threshold != null) {            return ResponseEntity.ok(inventoryStockService.getLowStockItems(locationId, threshold));
        }
        return ResponseEntity.ok(inventoryStockService.getAllStock());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryStock> getStockById(@PathVariable Integer id) {        return inventoryStockService.getStockById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStock(@RequestBody InventoryStock stock) {
        try {            InventoryStock created = inventoryStockService.createStock(stock);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}