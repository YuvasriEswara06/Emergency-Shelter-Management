package com.example.demo.controller;

import com.example.demo.entity.InventoryLocations;
import com.example.demo.entity.InventoryStock;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.InventoryLocationService;
import com.example.demo.service.InventoryStockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RestController
@RequestMapping("/api/inventory-stock")
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;
    private final InventoryLocationService inventoryLocationService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public InventoryStockController(InventoryStockService inventoryStockService,
                                   InventoryLocationService inventoryLocationService,
                                   SecurityAuthorizationService securityAuthorizationService) {
        this.inventoryStockService = inventoryStockService;
        this.inventoryLocationService = inventoryLocationService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryStock>> getStock(Authentication authentication,
                                                       @RequestParam(required = false) Integer locationId,
                                                       @RequestParam(required = false) Integer threshold) {
        if (!securityAuthorizationService.isAdmin(authentication)) {
            Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
            if (assignedShelterId == null) {
                throw new AccessDeniedException("Staff user is not assigned to a shelter");
            }
            if (locationId != null) {
                InventoryLocations location = inventoryLocationService.getLocationById(locationId).orElse(null);
                if (location == null) {
                    return ResponseEntity.notFound().build();
                }
                securityAuthorizationService.assertShelterAccess(authentication,
                        location.getShelter() == null ? null : location.getShelter().getShelterId());
            }
        }

        if (locationId != null && threshold != null) {
            return ResponseEntity.ok(inventoryStockService.getLowStockItems(locationId, threshold));
        }

        if (securityAuthorizationService.isAdmin(authentication)) {
            return ResponseEntity.ok(inventoryStockService.getAllStock());
        }

        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        return ResponseEntity.ok(inventoryStockService.getAllStock().stream()
                .filter(s -> s.getLocation() != null && s.getLocation().getShelter() != null
                        && assignedShelterId.equals(s.getLocation().getShelter().getShelterId()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryStock> getStockById(Authentication authentication, @PathVariable Integer id) {
        InventoryStock stock = inventoryStockService.getStockById(id).orElse(null);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        if (!securityAuthorizationService.isAdmin(authentication)) {
            if (stock.getLocation() == null || stock.getLocation().getShelter() == null) {
                throw new AccessDeniedException("Forbidden: stock is outside your assigned shelter");
            }
            securityAuthorizationService.assertShelterAccess(authentication, stock.getLocation().getShelter().getShelterId());
        }
        return ResponseEntity.ok(stock);
    }

    @PostMapping
    public ResponseEntity<?> createStock(Authentication authentication, @RequestBody InventoryStock stock) {
        try {
            if (!securityAuthorizationService.isAdmin(authentication)) {
                if (stock == null || stock.getLocation() == null || stock.getLocation().getShelter() == null || stock.getLocation().getShelter().getShelterId() == null) {
                    throw new AccessDeniedException("Staff users must create stock for their assigned shelter");
                }
                securityAuthorizationService.assertShelterAccess(authentication, stock.getLocation().getShelter().getShelterId());
            }
            InventoryStock created = inventoryStockService.createStock(stock);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
