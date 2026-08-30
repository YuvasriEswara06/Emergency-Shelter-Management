package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_locations")
public class InventoryLocations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(name = "location_type", nullable = false, length = 10)
    private String locationType;

    @ManyToOne
    @JoinColumn(name = "shelter_id", unique = true)
    private Shelter shelter;

    public InventoryLocations() {
        // JPA
    }

    public InventoryLocations(String locationName, String locationType) {
        this.locationName = locationName;
        this.locationType = locationType;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Shelter getShelter() {
        return shelter;
    }

    public void setShelter(Shelter shelter) {
        this.shelter = shelter;
    }
}