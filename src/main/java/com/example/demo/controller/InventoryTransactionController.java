package com.example.demo.controller;

import com.example.demo.entity.InventoryTransactions;
import com.example.demo.security.SecurityAuthorizationService;
import com.example.demo.service.InventoryTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RestController
@RequestMapping("/api/inventory-transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public InventoryTransactionController(InventoryTransactionService inventoryTransactionService,
                                         SecurityAuthorizationService securityAuthorizationService) {
        this.inventoryTransactionService = inventoryTransactionService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryTransactions>> getTransactions(Authentication authentication,
                                                                     @RequestParam(required = false) Integer itemId,
                                                                     @RequestParam(required = false) Integer locationId) {
        List<InventoryTransactions> transactions;
        if (itemId != null) {
            transactions = inventoryTransactionService.getTransactionsByItem(itemId);
        } else if (locationId != null) {
            transactions = inventoryTransactionService.getTransactionsByLocation(locationId);
        } else {
            transactions = inventoryTransactionService.getAllTransactions();
        }

        if (securityAuthorizationService.isAdmin(authentication)) {
            return ResponseEntity.ok(transactions);
        }

        Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
        if (assignedShelterId == null) {
            throw new AccessDeniedException("Staff user is not assigned to a shelter");
        }

        List<InventoryTransactions> filtered = transactions.stream()
                .filter(t -> isTransactionInShelter(t, assignedShelterId))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransactions> getTransactionById(Authentication authentication, @PathVariable Integer id) {
        InventoryTransactions transaction = inventoryTransactionService.getTransactionById(id).orElse(null);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        if (!securityAuthorizationService.isAdmin(authentication)) {
            Integer assignedShelterId = securityAuthorizationService.getCurrentUserShelterId(authentication);
            if (assignedShelterId == null) {
                throw new AccessDeniedException("Staff user is not assigned to a shelter");
            }
            if (!isTransactionInShelter(transaction, assignedShelterId)) {
                throw new AccessDeniedException("Forbidden: transaction is outside your assigned shelter");
            }
        }
        return ResponseEntity.ok(transaction);
    }

    private boolean isTransactionInShelter(InventoryTransactions tx, Integer shelterId) {
        boolean fromMatch = tx.getFromLocation() != null && tx.getFromLocation().getShelter() != null
                && shelterId.equals(tx.getFromLocation().getShelter().getShelterId());
        boolean toMatch = tx.getToLocation() != null && tx.getToLocation().getShelter() != null
                && shelterId.equals(tx.getToLocation().getShelter().getShelterId());
        return fromMatch || toMatch;
    }
}
