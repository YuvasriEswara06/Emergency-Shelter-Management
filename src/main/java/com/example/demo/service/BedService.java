package com.example.demo.service;

import com.example.demo.entity.Beds;
import com.example.demo.repository.BedRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BedService {

    private final BedRepository bedRepository;

    public BedService(BedRepository bedRepository) {
        this.bedRepository = bedRepository;
    }

    public List<Beds> getAllBeds() {
        return bedRepository.findAll();
    }

    public Optional<Beds> getBedById(Integer bedId) {
        return bedRepository.findById(bedId);
    }

    public List<Beds> getBedsByShelter(Integer shelterId) {
        // Repository does not expose a direct findByShelterId; filter in-memory to follow project pattern without changing repository interfaces
        return bedRepository.findAll().stream()
                .filter(b -> b.getShelter() != null && shelterId.equals(b.getShelter().getShelterId()))
                .collect(Collectors.toList());
    }

    public List<Beds> getAvailableBedsByShelter(Integer shelterId) {
        return bedRepository.findByShelterShelterIdAndStatus(shelterId, "Available");
    }

    public Beds createBed(Beds bed) {
        if (bed.getBedNumber() == null || bed.getBedNumber() <= 0) {
            throw new IllegalArgumentException("Bed number must be a positive integer");
        }
        if (bed.getShelter() == null || bed.getShelter().getShelterId() == null) {
            throw new IllegalArgumentException("Bed must be associated with a shelter");
        }
        return bedRepository.save(bed);
    }
}