package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "volunteers")
public class Volunteers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "volunteer_id")
    private Integer volunteerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "skill", length = 50)
    private String skill;

    @Column(name = "availability", nullable = false, length = 15)
    private String availability = "Available";

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    public Volunteers() {
        // JPA
    }

    public Volunteers(String name, String phone, String skill) {
        this.name = name;
        this.phone = phone;
        this.skill = skill;
        this.availability = "Available";
    }

    public Integer getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Integer volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Shelter getShelter() {
        return shelter;
    }

    public void setShelter(Shelter shelter) {
        this.shelter = shelter;
    }
}