package com.example.demo.repository;

import com.example.demo.entity.Victims;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VictimRepository extends JpaRepository<Victims, Integer> {

    List<Victims> findByNameContainingIgnoreCase(String namePart);
}