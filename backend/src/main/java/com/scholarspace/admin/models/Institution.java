package com.scholarspace.admin.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "institutions")
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institution_id")
    private Long institutionId;
    
    @Column(nullable = false)
    private String name;
    
    private String location;
    
    private String contact;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Default constructor
    public Institution() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with fields
    public Institution(String name, String location, String contact) {
        this.name = name;
        this.location = location;
        this.contact = contact;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}