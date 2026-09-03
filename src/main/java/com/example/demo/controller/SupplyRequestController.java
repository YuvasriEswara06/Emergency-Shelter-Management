package com.example.demo.controller;

import com.example.demo.entity.SupplyRequests;
import com.example.demo.service.SupplyRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supply-requests")
public class SupplyRequestController {

    private final SupplyRequestService supplyRequestService;

    public SupplyRequestController(SupplyRequestService supplyRequestService) {
        this.supplyRequestService = supplyRequestService;
    }

    // GET /api/supply-requests?shelterId=3
    // GET /api/supply-requests?pending=true
    // GET /api/supply-requests
    @GetMapping
    public ResponseEntity<?> getRequests(
            @RequestParam(required = false) Integer shelterId,
            @RequestParam(required = false, defaultValue = "false") Boolean pending) {

        if (shelterId != null) {
            return ResponseEntity.ok(supplyRequestService.getRequestsByShelter(shelterId));
        }
        if (pending != null && pending) {
            return ResponseEntity.ok(supplyRequestService.getPendingRequests());
        }
        return ResponseEntity.ok(supplyRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplyRequests> getRequestById(@PathVariable Integer id) {
        return supplyRequestService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/supply-requests
    // body: { "shelterId": 1, "itemId": 2, "quantityRequested": 10, "priority": "High" }
    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> body) {
        try {
            Integer shelterId = parseInteger(body.get("shelterId"));
            Integer itemId = parseInteger(body.get("itemId"));
            Integer quantityRequested = parseInteger(body.get("quantityRequested"));
            String priority = body.get("priority") == null ? "Medium" : body.get("priority").toString();

            SupplyRequests created = supplyRequestService.createRequest(shelterId, itemId, quantityRequested, priority);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/supply-requests/5/approve
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

    // PATCH /api/supply-requests/5/reject
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

    // PATCH /api/supply-requests/5/fulfill?userId=1
    @PatchMapping("/{id}/fulfill")
    public ResponseEntity<?> fulfillRequest(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(supplyRequestService.fulfillRequest(id, userId));
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
