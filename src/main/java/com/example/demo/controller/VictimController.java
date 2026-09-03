package com.example.demo.controller;

import com.example.demo.entity.Victims;
import com.example.demo.service.VictimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/victims")
public class VictimController {

    private final VictimService victimService;

    public VictimController(VictimService victimService) {
        this.victimService = victimService;
    }

    @GetMapping
    public ResponseEntity<List<Victims>> getVictims(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(victimService.searchVictimsByName(search));
        }
        return ResponseEntity.ok(victimService.getAllVictims());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Victims> getVictimById(@PathVariable Integer id) {
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
}