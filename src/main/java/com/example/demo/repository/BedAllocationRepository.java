package com.example.demo.repository;

import com.example.demo.entity.BedAllocations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedAllocationRepository extends JpaRepository<BedAllocations, Integer> {

    Optional<BedAllocations> findByBedBedIdAndAllocationStatus(Integer bedId, String allocationStatus);

    List<BedAllocations> findByVictimVictimId(Integer victimId);
}