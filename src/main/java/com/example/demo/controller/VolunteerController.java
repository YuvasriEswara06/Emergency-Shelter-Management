package com.example.demo.controller;

import com.example.demo.entity.Volunteers;
import com.example.demo.service.VolunteerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping
    public ResponseEntity<List<Volunteers>> getVolunteers(@RequestParam(required = false) Integer shelterId,
                                                          @RequestParam(required = false) String availability) {
        if (shelterId != null) {
            return ResponseEntity.ok(volunteerService.getVolunteersByShelter(shelterId));
        }
        if (availability != null && !availability.isBlank()) {
            return ResponseEntity.ok(volunteerService.getVolunteersByAvailability(availability));
        }
        return ResponseEntity.ok(volunteerService.getAllVolunteers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Volunteers> getVolunteerById(@PathVariable Integer id) {
        return volunteerService.getVolunteerById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createVolunteer(@RequestBody Volunteers volunteer) {
        try {
            Volunteers created = volunteerService.createVolunteer(volunteer);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}