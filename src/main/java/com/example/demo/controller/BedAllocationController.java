package com.example.demo.controller;

import com.example.demo.entity.BedAllocations;
import com.example.demo.service.BedAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bed-allocations")
public class BedAllocationController {

    private final BedAllocationService bedAllocationService;

    public BedAllocationController(BedAllocationService bedAllocationService) {
        this.bedAllocationService = bedAllocationService;
    }

    // GET /api/bed-allocations?bedId=3 -> active allocation for a bed
    // GET /api/bed-allocations?victimId=7 -> allocation history for a victim
    @GetMapping
    public ResponseEntity<?> getAllocations(
            @RequestParam(required = false) Integer bedId,
            @RequestParam(required = false) Integer victimId) {

        if (bedId != null) {
            return bedAllocationService.getActiveAllocationForBed(bedId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        if (victimId != null) {
            List<BedAllocations> history = bedAllocationService.getAllocationHistoryForVictim(victimId);
            return ResponseEntity.ok(history);
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Provide either bedId or victimId"));
    }
}
