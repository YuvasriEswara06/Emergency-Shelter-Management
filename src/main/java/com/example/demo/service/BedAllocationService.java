package com.example.demo.service;

import com.example.demo.entity.Beds;
import com.example.demo.entity.BedAllocations;
import com.example.demo.entity.Victims;
import com.example.demo.repository.BedAllocationRepository;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.VictimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BedAllocationService {

    private final BedAllocationRepository bedAllocationRepository;
    private final BedRepository bedRepository;
    private final VictimRepository victimRepository;

    public BedAllocationService(BedAllocationRepository bedAllocationRepository,
                                BedRepository bedRepository,
                                VictimRepository victimRepository) {
        this.bedAllocationRepository = bedAllocationRepository;
        this.bedRepository = bedRepository;
        this.victimRepository = victimRepository;
    }

    /**
     * Allocates a bed to a victim.
     * This is @Transactional because it touches TWO tables (Bed_Allocations and Beds)
     * and both must succeed together, or neither should happen.
     */
    @Transactional
    public BedAllocations allocateBed(Integer bedId, Integer victimId) {

        Beds bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new IllegalArgumentException("Bed not found: " + bedId));

        Victims victim = victimRepository.findById(victimId)
                .orElseThrow(() -> new IllegalArgumentException("Victim not found: " + victimId));

        // === THE CORE BUSINESS RULE ===
        // Check if this bed already has an ACTIVE allocation before creating a new one.
        // This is the check that prevents two victims being assigned the same bed.
        Optional<BedAllocations> existingActive =
                bedAllocationRepository.findByBedBedIdAndAllocationStatus(bedId, "Active");

        if (existingActive.isPresent()) {
            throw new IllegalStateException(
                    "Bed " + bedId + " already has an active allocation (allocation_id="
                            + existingActive.get().getAllocationId() + "). Vacate it first.");
        }

        if (!"Available".equals(bed.getStatus())) {
            throw new IllegalStateException("Bed " + bedId + " is not marked Available (current status: "
                    + bed.getStatus() + ")");
        }

        // Create the allocation record
        BedAllocations allocation = new BedAllocations(bed, victim);
        bedAllocationRepository.save(allocation);

        // Update the bed's own status too — both changes happen in the same transaction
        bed.setStatus("Occupied");
        bedRepository.save(bed);

        return allocation;
    }

    /**
     * Vacates an existing active allocation — frees the bed.
     * Also, @Transactional for the same reason: two tables change together.
     */
    @Transactional
    public BedAllocations vacateBed(Integer allocationId) {

        BedAllocations allocation = bedAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Allocation not found: " + allocationId));

        if ("Vacated".equals(allocation.getAllocationStatus())) {
            throw new IllegalStateException("Allocation " + allocationId + " is already vacated");
        }

        // allocation_status and vacated_date are updated TOGETHER, as required by our design
        allocation.setAllocationStatus("Vacated");
        allocation.setVacatedDate(LocalDateTime.now());
        bedAllocationRepository.save(allocation);

        Beds bed = allocation.getBed();
        bed.setStatus("Available");
        bedRepository.save(bed);

        return allocation;
    }

    public Optional<BedAllocations> getActiveAllocationForBed(Integer bedId) {
        return bedAllocationRepository.findByBedBedIdAndAllocationStatus(bedId, "Active");
    }

    public List<BedAllocations> getAllocationHistoryForVictim(Integer victimId) {
        return bedAllocationRepository.findByVictimVictimId(victimId);
    }
}