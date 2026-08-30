package com.example.demo.repository;

import com.example.demo.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Integer> {

    Optional<InventoryStock> findByItemItemIdAndLocationLocationId(Integer itemId, Integer locationId);

    List<InventoryStock> findByLocationLocationIdAndQuantityLessThan(Integer locationId, Integer threshold);
}