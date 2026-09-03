package com.example.demo.controller;

import com.example.demo.entity.Beds;
import com.example.demo.service.BedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/beds")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping
    public ResponseEntity<List<Beds>> getBeds(
            @RequestParam(required = false) Integer shelterId,
            @RequestParam(required = false) Boolean available) {
        if (shelterId != null) {
            if (available != null && available) {
                return ResponseEntity.ok(bedService.getAvailableBedsByShelter(shelterId));
            }
            return ResponseEntity.ok(bedService.getBedsByShelter(shelterId));
        }
        return ResponseEntity.ok(bedService.getAllBeds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beds> getBedById(@PathVariable Integer id) {
        return bedService.getBedById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBed(@RequestBody Beds bed) {
        try {
            Beds created = bedService.createBed(bed);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}