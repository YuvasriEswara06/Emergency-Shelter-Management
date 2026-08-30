package com.example.demo.repository;

import com.example.demo.entity.Volunteers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteersRepository extends JpaRepository<Volunteers, Integer> {

    List<Volunteers> findByAvailability(String availability);

    List<Volunteers> findByShelterShelterId(Integer shelterId);
}