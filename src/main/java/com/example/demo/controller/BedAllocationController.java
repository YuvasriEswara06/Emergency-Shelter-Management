package com.example.demo.controller;

import com.example.demo.entity.BedAllocations;
import com.example.demo.entity.Beds;
import com.example.demo.entity.Victims;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.BedAllocationService;
import com.example.demo.service.BedService;
import com.example.demo.service.VictimService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RestController
@RequestMapping("/api/bed-allocations")
public class BedAllocationController {

    private final BedAllocationService bedAllocationService;
    private final BedService bedService;
    private final VictimService victimService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public BedAllocationController(BedAllocationService bedAllocationService,
                                  BedService bedService,
                                  VictimService victimService,
                                  SecurityAuthorizationService securityAuthorizationService) {
        this.bedAllocationService = bedAllocationService;
        this.bedService = bedService;
        this.victimService = victimService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<?> getAllocations(Authentication authentication,
                                          @RequestParam(required = false) Integer bedId,
                                          @RequestParam(required = false) Integer victimId) {

        if (bedId != null) {
            Beds bed = bedService.getBedById(bedId).orElse(null);
            if (bed == null) {
                return ResponseEntity.notFound().build();
            }
            if (!securityAuthorizationService.isAdmin(authentication)) {
                securityAuthorizationService.assertShelterAccess(authentication,
                        bed.getShelter() == null ? null : bed.getShelter().getShelterId());
            }
            return bedAllocationService.getActiveAllocationForBed(bedId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        if (victimId != null) {
            Victims victim = victimService.getVictimById(victimId).orElse(null);
            if (victim == null) {
                return ResponseEntity.notFound().build();
            }
            if (!securityAuthorizationService.isAdmin(authentication)) {
                Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
                if (assignedShelterId == null) {
                    throw new AccessDeniedException("Staff user is not assigned to a shelter");
                }
                boolean allowed = bedAllocationService.getAllocationHistoryForVictim(victimId).stream()
                        .filter(a -> a.getBed() != null && a.getBed().getShelter() != null)
                        .anyMatch(a -> assignedShelterId.equals(a.getBed().getShelter().getShelterId()));
                if (!allowed) {
                    throw new AccessDeniedException("Forbidden: victim is not in your assigned shelter");
                }
            }
            List<BedAllocations> history = bedAllocationService.getAllocationHistoryForVictim(victimId);
            return ResponseEntity.ok(history);
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Provide either bedId or victimId"));
    }
}
