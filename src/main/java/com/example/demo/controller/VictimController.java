package com.example.demo.controller;

import com.example.demo.entity.Victims;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.BedAllocationService;
import com.example.demo.service.VictimService;
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
@RequestMapping("/api/victims")
public class VictimController {

    private final VictimService victimService;
    private final BedAllocationService bedAllocationService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public VictimController(VictimService victimService,
                           BedAllocationService bedAllocationService,
                           SecurityAuthorizationService securityAuthorizationService) {
        this.victimService = victimService;
        this.bedAllocationService = bedAllocationService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<Victims>> getVictims(Authentication authentication,
                                                   @RequestParam(required = false) String search) {
        List<Victims> victims;
        if (search != null && !search.isBlank()) {
            victims = victimService.searchVictimsByName(search);
        } else {
            victims = victimService.getAllVictims();
        }

        if (securityAuthorizationService.isAdmin(authentication)) {
            return ResponseEntity.ok(victims);
        }

        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        if (assignedShelterId == null) {
            throw new AccessDeniedException("Staff user is not assigned to a shelter");
        }

        List<Victims> filtered = victims.stream()
                .filter(v -> isVictimInShelter(authentication, v.getVictimId()))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Victims> getVictimById(Authentication authentication, @PathVariable Integer id) {
        if (!securityAuthorizationService.isAdmin(authentication)) {
            if (!isVictimInShelter(authentication, id)) {
                throw new AccessDeniedException("Forbidden: victim is not in your assigned shelter");
            }
        }
        return victimService.getVictimById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createVictim(@RequestBody Victims victim) {
        try {
            Victims created = victimService.createVictim(victim);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private boolean isVictimInShelter(Authentication authentication, Integer victimId) {
        if (securityAuthorizationService.isAdmin(authentication)) {
            return true;
        }
        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        if (assignedShelterId == null) {
            return false;
        }
        return bedAllocationService.getAllocationHistoryForVictim(victimId).stream()
                .filter(a -> a.getBed() != null && a.getBed().getShelter() != null)
                .anyMatch(a -> assignedShelterId.equals(a.getBed().getShelter().getShelterId()));
    }
}