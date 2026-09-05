package com.example.demo.controller;

import com.example.demo.entity.Shelter;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.ShelterService;
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
@RequestMapping("/api/shelters")
public class ShelterController {

    private final ShelterService shelterService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public ShelterController(ShelterService shelterService, SecurityAuthorizationService securityAuthorizationService) {
        this.shelterService = shelterService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    // GET /api/shelters              -> all shelters
    // GET /api/shelters?status=Active -> filter by status
    // GET /api/shelters?search=Adyar  -> search by name
    @GetMapping
    public ResponseEntity<List<Shelter>> getShelters(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        if (securityAuthorizationService.isAdmin(authentication)) {
            if (search != null && !search.isBlank()) {
                return ResponseEntity.ok(shelterService.searchSheltersByName(search));
            }
            if (status != null && !status.isBlank()) {
                return ResponseEntity.ok(shelterService.getSheltersByStatus(status));
            }
            return ResponseEntity.ok(shelterService.getAllShelters());
        }

        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        if (assignedShelterId == null) {
            throw new AccessDeniedException("Staff user is not assigned to a shelter");
        }
        return shelterService.getShelterById(assignedShelterId)
                .map(s -> ResponseEntity.ok(List.of(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/shelters/3
    @GetMapping("/{id}")
    public ResponseEntity<Shelter> getShelterById(Authentication authentication, @PathVariable Integer id) {
        if (!securityAuthorizationService.isAdmin(authentication)) {
            securityAuthorizationService.assertShelterAccess(authentication, id);
        }
        return shelterService.getShelterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/shelters
    // body: { "name": "...", "location": "...", "capacity": 10 }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createShelter(@RequestBody Shelter shelter) {
        try {
            Shelter created = shelterService.createShelter(shelter);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/shelters/3/status
    // body: { "status": "Inactive" }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateShelterStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            Shelter updated = shelterService.updateShelterStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}