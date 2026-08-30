package com.example.demo.service;

import com.example.demo.entity.Victims;
import com.example.demo.repository.VictimRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VictimService {

    private final VictimRepository victimRepository;

    public VictimService(VictimRepository victimRepository) {
        this.victimRepository = victimRepository;
    }

    public List<Victims> getAllVictims() {
        return victimRepository.findAll();
    }

    public Optional<Victims> getVictimById(Integer victimId) {
        return victimRepository.findById(victimId);
    }

    public List<Victims> searchVictimsByName(String namePart) {
        return victimRepository.findByNameContainingIgnoreCase(namePart);
    }

    public Victims createVictim(Victims victim) {
        if (victim.getName() == null || victim.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Victim name must not be empty");
        }
        return victimRepository.save(victim);
    }
}