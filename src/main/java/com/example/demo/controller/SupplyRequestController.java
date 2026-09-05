package com.example.demo.controller;

import com.example.demo.entity.SupplyRequests;
import com.example.demo.security.CustomUserPrincipal;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.SupplyRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RestController
@RequestMapping("/api/supply-requests")
public class SupplyRequestController {

    private final SupplyRequestService supplyRequestService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public SupplyRequestController(SupplyRequestService supplyRequestService,
                                 SecurityAuthorizationService securityAuthorizationService) {
        this.supplyRequestService = supplyRequestService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<?> getRequests(Authentication authentication,
                                        @RequestParam(required = false) Integer shelterId,
                                        @RequestParam(required = false, defaultValue = "false") Boolean pending) {

        if (!securityAuthorizationService.isAdmin(authentication)) {
            Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
            if (assignedShelterId == null) {
                throw new AccessDeniedException("Staff user is not assigned to a shelter");
            }
            if (shelterId != null) {
                securityAuthorizationService.assertShelterAccess(authentication, shelterId);
                if (pending) {
                    return ResponseEntity.ok(supplyRequestService.getPendingRequests().stream()
                            .filter(r -> r.getShelter() != null && assignedShelterId.equals(r.getShelter().getShelterId()))
                            .toList());
                }
                return ResponseEntity.ok(supplyRequestService.getRequestsByShelter(shelterId));
            }
            List<SupplyRequests> ownRequests = supplyRequestService.getAllRequests().stream()
                    .filter(r -> r.getShelter() != null && assignedShelterId.equals(r.getShelter().getShelterId()))
                    .toList();
            if (pending) {
                ownRequests = ownRequests.stream()
                        .filter(r -> "Pending".equals(r.getStatus()))
                        .toList();
            }
            return ResponseEntity.ok(ownRequests);
        }

        if (shelterId != null) {
            return ResponseEntity.ok(supplyRequestService.getRequestsByShelter(shelterId));
        }
        if (pending) {
            return ResponseEntity.ok(supplyRequestService.getPendingRequests());
        }
        return ResponseEntity.ok(supplyRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(Authentication authentication, @PathVariable Integer id) {
        SupplyRequests request = supplyRequestService.getRequestById(id).orElse(null);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        if (!securityAuthorizationService.isAdmin(authentication)) {
            if (request.getShelter() == null || request.getShelter().getShelterId() == null) {
                throw new AccessDeniedException("Forbidden: request is outside your assigned shelter");
            }
            securityAuthorizationService.assertShelterAccess(authentication, request.getShelter().getShelterId());
        }
        return ResponseEntity.ok(request);
    }

    @PostMapping
    public ResponseEntity<?> createRequest(Authentication authentication, @RequestBody Map<String, Object> body) {
        try {
            Integer shelterId = parseInteger(body.get("shelterId"));
            if (!securityAuthorizationService.isAdmin(authentication)) {
                securityAuthorizationService.assertShelterAccess(authentication, shelterId);
            }
            Integer itemId = parseInteger(body.get("itemId"));
            Integer quantityRequested = parseInteger(body.get("quantityRequested"));
            String priority = body.get("priority") == null ? "Medium" : body.get("priority").toString();

            SupplyRequests created = supplyRequestService.createRequest(shelterId, itemId, quantityRequested, priority);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(supplyRequestService.approveRequest(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(supplyRequestService.rejectRequest(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/fulfill")
    public ResponseEntity<?> fulfillRequest(@PathVariable Integer id,
                                           @AuthenticationPrincipal CustomUserPrincipal principal) {
        try {
            Integer actingUserId = principal.getUserId();
            return ResponseEntity.ok(supplyRequestService.fulfillRequest(id, actingUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required integer field");
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }
}
