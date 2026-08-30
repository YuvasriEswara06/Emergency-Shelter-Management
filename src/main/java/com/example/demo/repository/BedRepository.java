package com.example.demo.repository;

import com.example.demo.entity.Beds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Beds, Integer> {

    List<Beds> findByShelterShelterIdAndStatus(Integer shelterId, String status);
}