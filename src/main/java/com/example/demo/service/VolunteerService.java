package com.example.demo.service;

import com.example.demo.entity.Volunteers;
import com.example.demo.repository.VolunteersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VolunteerService {

    private final VolunteersRepository volunteersRepository;

    public VolunteerService(VolunteersRepository volunteersRepository) {
        this.volunteersRepository = volunteersRepository;
    }

    public List<Volunteers> getAllVolunteers() {
        return volunteersRepository.findAll();
    }

    public Optional<Volunteers> getVolunteerById(Integer volunteerId) {
        return volunteersRepository.findById(volunteerId);
    }

    public List<Volunteers> getVolunteersByAvailability(String availability) {
        return volunteersRepository.findByAvailability(availability);
    }

    public List<Volunteers> getVolunteersByShelter(Integer shelterId) {
        return volunteersRepository.findByShelterShelterId(shelterId);
    }

    public Volunteers createVolunteer(Volunteers volunteer) {
        if (volunteer.getName() == null || volunteer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Volunteer name must not be empty");
        }
        return volunteersRepository.save(volunteer);
    }
}