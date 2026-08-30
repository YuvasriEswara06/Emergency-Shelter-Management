package com.example.demo.service;

import com.example.demo.entity.InventoryStock;
import com.example.demo.repository.InventoryStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;

    public InventoryStockService(InventoryStockRepository inventoryStockRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
    }

    public List<InventoryStock> getAllStock() {
        return inventoryStockRepository.findAll();
    }

    public Optional<InventoryStock> getStockById(Integer stockId) {
        return inventoryStockRepository.findById(stockId);
    }

    public Optional<InventoryStock> findByItemAndLocation(Integer itemId, Integer locationId) {
        return inventoryStockRepository.findByItemItemIdAndLocationLocationId(itemId, locationId);
    }

    public List<InventoryStock> getLowStockItems(Integer locationId, Integer threshold) {
        return inventoryStockRepository.findByLocationLocationIdAndQuantityLessThan(locationId, threshold);
    }

    public InventoryStock createStock(InventoryStock stock) {
        if (stock.getQuantity() == null || stock.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        if (stock.getLowStockThreshold() == null || stock.getLowStockThreshold() < 0) {
            throw new IllegalArgumentException("Low stock threshold must be non-negative");
        }
        return inventoryStockRepository.save(stock);
    }
}