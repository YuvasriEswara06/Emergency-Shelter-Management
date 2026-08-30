package com.example.demo.service;

import com.example.demo.entity.Shelter;
import com.example.demo.repository.ShelterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShelterService {

    private final ShelterRepository shelterRepository;

    // Constructor injection (preferred over @Autowired on fields)
    public ShelterService(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    public List<Shelter> getAllShelters() {
        return shelterRepository.findAll();
    }

    public Optional<Shelter> getShelterById(Integer shelterId) {
        return shelterRepository.findById(shelterId);
    }

    public List<Shelter> searchSheltersByName(String namePart) {
        return shelterRepository.findByNameContainingIgnoreCase(namePart);
    }

    public List<Shelter> getSheltersByStatus(String status) {
        return shelterRepository.findByStatus(status);
    }

    public Shelter createShelter(Shelter shelter) {
        // Basic validation beyond what the DB CHECK already guarantees
        if (shelter.getCapacity() == null || shelter.getCapacity() <= 0) {
            throw new IllegalArgumentException("Shelter capacity must be greater than 0");
        }
        return shelterRepository.save(shelter);
    }

    public Shelter updateShelterStatus(Integer shelterId, String newStatus) {
        Shelter shelter = shelterRepository.findById(shelterId)
                .orElseThrow(() -> new IllegalArgumentException("Shelter not found: " + shelterId));
        shelter.setStatus(newStatus);
        return shelterRepository.save(shelter);
    }
}