package com.example.demo.service;

import com.example.demo.entity.Items;
import com.example.demo.repository.ItemsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemsRepository itemsRepository;

    public ItemService(ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
    }

    public List<Items> getAllItems() {
        return itemsRepository.findAll();
    }

    public Optional<Items> getItemById(Integer itemId) {
        return itemsRepository.findById(itemId);
    }

    public Optional<Items> findByItemName(String itemName) {
        return itemsRepository.findByItemName(itemName);
    }

    public Items createItem(Items item) {
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name must not be empty");
        }
        if (item.getUnit() == null || item.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit must not be empty");
        }
        return itemsRepository.save(item);
    }
}