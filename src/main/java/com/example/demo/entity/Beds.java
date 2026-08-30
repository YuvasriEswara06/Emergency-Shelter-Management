package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "beds")
public class Beds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bed_id")
    private Integer bedId;

    @ManyToOne
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @Column(name = "bed_number", nullable = false)
    private Integer bedNumber;

    @Column(name = "status", nullable = false, length = 10)
    private String status = "Available";

    public Beds() {
        // JPA
    }

    public Beds(Shelter shelter, Integer bedNumber) {
        this.shelter = shelter;
        this.bedNumber = bedNumber;
        this.status = "Available";
    }

    public Integer getBedId() {
        return bedId;
    }

    public void setBedId(Integer bedId) {
        this.bedId = bedId;
    }

    public Shelter getShelter() {
        return shelter;
    }

    public void setShelter(Shelter shelter) {
        this.shelter = shelter;
    }

    public Integer getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(Integer bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}