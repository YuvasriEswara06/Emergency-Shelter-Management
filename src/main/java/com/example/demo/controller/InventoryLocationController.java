package com.example.demo.controller;

import com.example.demo.entity.InventoryLocations;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.InventoryLocationService;
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
@RequestMapping("/api/inventory-locations")
public class InventoryLocationController {

    private final InventoryLocationService inventoryLocationService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public InventoryLocationController(InventoryLocationService inventoryLocationService,
                                      SecurityAuthorizationService securityAuthorizationService) {
        this.inventoryLocationService = inventoryLocationService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryLocations>> getLocations(Authentication authentication) {
        if (securityAuthorizationService.isAdmin(authentication)) {
            return ResponseEntity.ok(inventoryLocationService.getAllLocations());
        }
        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        if (assignedShelterId == null) {
            throw new AccessDeniedException("Staff user is not assigned to a shelter");
        }
        return ResponseEntity.ok(inventoryLocationService.getAllLocations().stream()
                .filter(l -> l.getShelter() != null && assignedShelterId.equals(l.getShelter().getShelterId()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryLocations> getLocationById(Authentication authentication, @PathVariable Integer id) {
        InventoryLocations location = inventoryLocationService.getLocationById(id).orElse(null);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        if (!securityAuthorizationService.isAdmin(authentication)) {
            securityAuthorizationService.assertShelterAccess(authentication,
                    location.getShelter() == null ? null : location.getShelter().getShelterId());
        }
        return ResponseEntity.ok(location);
    }

    @PostMapping
    public ResponseEntity<?> createLocation(Authentication authentication, @RequestBody InventoryLocations location) {
        try {
            if (!securityAuthorizationService.isAdmin(authentication)) {
                if (location == null || location.getShelter() == null || location.getShelter().getShelterId() == null) {
                    throw new AccessDeniedException("Staff users must create inventory locations for their assigned shelter");
                }
                securityAuthorizationService.assertShelterAccess(authentication, location.getShelter().getShelterId());
            }
            InventoryLocations created = inventoryLocationService.createLocation(location);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}