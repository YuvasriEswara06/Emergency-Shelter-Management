package com.example.demo.repository;

import com.example.demo.entity.InventoryTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionsRepository extends JpaRepository<InventoryTransactions, Integer> {

    List<InventoryTransactions> findByItemItemId(Integer itemId);

    List<InventoryTransactions> findByFromLocationLocationIdOrToLocationLocationId(Integer fromId, Integer toId);
}