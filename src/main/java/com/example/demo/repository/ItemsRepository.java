package com.example.demo.repository;

import com.example.demo.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Integer> {

    Optional<Items> findByItemName(String itemName);
}