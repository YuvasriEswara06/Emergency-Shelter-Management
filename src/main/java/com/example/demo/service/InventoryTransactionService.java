package com.example.demo.service;

import com.example.demo.entity.InventoryTransactions;
import com.example.demo.repository.InventoryTransactionsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryTransactionService {

    private final InventoryTransactionsRepository inventoryTransactionsRepository;

    public InventoryTransactionService(InventoryTransactionsRepository inventoryTransactionsRepository) {
        this.inventoryTransactionsRepository = inventoryTransactionsRepository;
    }

    public List<InventoryTransactions> getAllTransactions() {
        return inventoryTransactionsRepository.findAll();
    }

    public Optional<InventoryTransactions> getTransactionById(Integer transactionId) {
        return inventoryTransactionsRepository.findById(transactionId);
    }

    public List<InventoryTransactions> getTransactionsByItem(Integer itemId) {
        return inventoryTransactionsRepository.findByItemItemId(itemId);
    }

    public List<InventoryTransactions> getTransactionsByLocation(Integer locationId) {
        return inventoryTransactionsRepository.findByFromLocationLocationIdOrToLocationLocationId(locationId, locationId);
    }

    // Creation and the transactional transfer logic is intentionally omitted; handled by SupplyRequestService elsewhere.
}