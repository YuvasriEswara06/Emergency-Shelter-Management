package com.example.demo.controller;

import com.example.demo.entity.Beds;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.BedService;
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
@RequestMapping("/api/beds")
public class BedController {

    private final BedService bedService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public BedController(BedService bedService, SecurityAuthorizationService securityAuthorizationService) {
        this.bedService = bedService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<Beds>> getBeds(Authentication authentication,
                                             @RequestParam(required = false) Integer shelterId,
                                             @RequestParam(required = false) Boolean available) {
        Integer effectiveShelterId = shelterId;
        if (!securityAuthorizationService.isAdmin(authentication)) {
            Integer assigned = securityAuthorizationService.getCurrentUserShelterId(authentication);
            if (assigned == null) {
                throw new AccessDeniedException("Staff user is not assigned to a shelter");
            }
            if (shelterId == null) {
                effectiveShelterId = assigned;
            } else {
                securityAuthorizationService.assertShelterAccess(authentication, shelterId);
            }
        }

        if (effectiveShelterId != null) {
            if (available != null && available) {
                return ResponseEntity.ok(bedService.getAvailableBedsByShelter(effectiveShelterId));
            }
            return ResponseEntity.ok(bedService.getBedsByShelter(effectiveShelterId));
        }
        return ResponseEntity.ok(bedService.getAllBeds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beds> getBedById(Authentication authentication, @PathVariable Integer id) {
        Beds bed = bedService.getBedById(id).orElse(null);
        if (bed == null) {
            return ResponseEntity.notFound().build();
        }
        if (!securityAuthorizationService.isAdmin(authentication)) {
            securityAuthorizationService.assertShelterAccess(authentication,
                    bed.getShelter() == null ? null : bed.getShelter().getShelterId());
        }
        return ResponseEntity.ok(bed);
    }

    @PostMapping
    public ResponseEntity<?> createBed(Authentication authentication, @RequestBody Beds bed) {
        try {
            if (!securityAuthorizationService.isAdmin(authentication)) {
                if (bed == null || bed.getShelter() == null || bed.getShelter().getShelterId() == null) {
                    throw new AccessDeniedException("Staff users must create beds for their assigned shelter");
                }
                securityAuthorizationService.assertShelterAccess(authentication, bed.getShelter().getShelterId());
            }
            Beds created = bedService.createBed(bed);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}