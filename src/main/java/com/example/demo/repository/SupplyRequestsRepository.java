package com.example.demo.repository;

import com.example.demo.entity.SupplyRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyRequestsRepository extends JpaRepository<SupplyRequests, Integer> {

    List<SupplyRequests> findByShelterShelterIdAndStatus(Integer shelterId, String status);

    List<SupplyRequests> findByStatus(String status);
}