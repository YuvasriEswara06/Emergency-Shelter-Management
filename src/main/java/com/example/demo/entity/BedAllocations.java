package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bed_allocations")
public class BedAllocations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Integer allocationId;

    @ManyToOne
    @JoinColumn(name = "bed_id", nullable = false)
    private Beds bed;

    @ManyToOne
    @JoinColumn(name = "victim_id", nullable = false)
    private Victims victim;

    @Column(name = "allocated_date", nullable = false)
    private LocalDateTime allocatedDate = LocalDateTime.now();

    @Column(name = "vacated_date")
    private LocalDateTime vacatedDate;

    @Column(name = "allocation_status", nullable = false, length = 10)
    private String allocationStatus = "Active";

    public BedAllocations() {
        // JPA
    }

    public BedAllocations(Beds bed, Victims victim) {
        this.bed = bed;
        this.victim = victim;
        this.allocatedDate = LocalDateTime.now();
        this.allocationStatus = "Active";
    }

    public Integer getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Integer allocationId) {
        this.allocationId = allocationId;
    }

    public Beds getBed() {
        return bed;
    }

    public void setBed(Beds bed) {
        this.bed = bed;
    }

    public Victims getVictim() {
        return victim;
    }

    public void setVictim(Victims victim) {
        this.victim = victim;
    }

    public LocalDateTime getAllocatedDate() {
        return allocatedDate;
    }

    public void setAllocatedDate(LocalDateTime allocatedDate) {
        this.allocatedDate = allocatedDate;
    }

    public LocalDateTime getVacatedDate() {
        return vacatedDate;
    }

    public void setVacatedDate(LocalDateTime vacatedDate) {
        this.vacatedDate = vacatedDate;
    }

    public String getAllocationStatus() {
        return allocationStatus;
    }

    public void setAllocationStatus(String allocationStatus) {
        this.allocationStatus = allocationStatus;
    }
}