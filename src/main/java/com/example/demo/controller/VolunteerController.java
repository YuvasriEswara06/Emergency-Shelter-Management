package com.example.demo.controller;

import com.example.demo.entity.Volunteers;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.VolunteerService;
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
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public VolunteerController(VolunteerService volunteerService,
                              SecurityAuthorizationService securityAuthorizationService) {
        this.volunteerService = volunteerService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<Volunteers>> getVolunteers(Authentication authentication,
                                                        @RequestParam(required = false) Integer shelterId,
                                                        @RequestParam(required = false) String availability) {
        if (securityAuthorizationService.isAdmin(authentication)) {
            if (shelterId != null) {
                return ResponseEntity.ok(volunteerService.getVolunteersByShelter(shelterId));
            }
            if (availability != null && !availability.isBlank()) {
                return ResponseEntity.ok(volunteerService.getVolunteersByAvailability(availability));
            }
            return ResponseEntity.ok(volunteerService.getAllVolunteers());
        }

        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        if (assignedShelterId == null) {
            throw new AccessDeniedException("Staff user is not assigned to a shelter");
        }
        if (shelterId != null) {
            securityAuthorizationService.assertShelterAccess(authentication, shelterId);
            return ResponseEntity.ok(volunteerService.getVolunteersByShelter(shelterId));
        }
        if (availability != null && !availability.isBlank()) {
            return ResponseEntity.ok(volunteerService.getVolunteersByAvailability(availability)
                    .stream()
                    .filter(v -> v.getShelter() != null && assignedShelterId.equals(v.getShelter().getShelterId()))
                    .toList());
        }
        return ResponseEntity.ok(volunteerService.getVolunteersByShelter(assignedShelterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Volunteers> getVolunteerById(Authentication authentication, @PathVariable Integer id) {
        Volunteers volunteer = volunteerService.getVolunteerById(id).orElse(null);
        if (volunteer == null) {
            return ResponseEntity.notFound().build();
        }
        if (!securityAuthorizationService.isAdmin(authentication)) {
            securityAuthorizationService.assertShelterAccess(authentication,
                    volunteer.getShelter() == null ? null : volunteer.getShelter().getShelterId());
        }
        return ResponseEntity.ok(volunteer);
    }

    @PostMapping
    public ResponseEntity<?> createVolunteer(Authentication authentication, @RequestBody Volunteers volunteer) {
        try {
            if (!securityAuthorizationService.isAdmin(authentication)) {
                if (volunteer == null || volunteer.getShelter() == null || volunteer.getShelter().getShelterId() == null) {
                    throw new AccessDeniedException("Staff users must create volunteers for their assigned shelter");
                }
                securityAuthorizationService.assertShelterAccess(authentication, volunteer.getShelter().getShelterId());
            }
            Volunteers created = volunteerService.createVolunteer(volunteer);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}