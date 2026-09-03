package com.example.demo.controller;

import com.example.demo.entity.InventoryTransactions;
import com.example.demo.service.InventoryTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    public InventoryTransactionController(InventoryTransactionService inventoryTransactionService) {
        this.inventoryTransactionService = inventoryTransactionService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryTransactions>> getTransactions(@RequestParam(required = false) Integer itemId,
                                                                        @RequestParam(required = false) Integer locationId) {
        if (itemId != null) {
            return ResponseEntity.ok(inventoryTransactionService.getTransactionsByItem(itemId));
        }
        if (locationId != null) {
            return ResponseEntity.ok(inventoryTransactionService.getTransactionsByLocation(locationId));
        }
        return ResponseEntity.ok(inventoryTransactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransactions> getTransactionById(@PathVariable Integer id) {
        return inventoryTransactionService.getTransactionById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}