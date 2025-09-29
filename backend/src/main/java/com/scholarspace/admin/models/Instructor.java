package com.scholarspace.admin.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "instructors")
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instructor_id")
    private Long instructorId;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    private String specialization;
    
    @Column(name = "office_location")
    private String officeLocation;
    
    @Column(name = "office_hours")
    private String officeHours;
    

    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Default constructor
    public Instructor() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with fields
    public Instructor(User user, Department department, String specialization, 
                     String officeLocation, String officeHours) {
        this.user = user;
        this.department = department;
        this.specialization = specialization;
        this.officeLocation = officeLocation;
        this.officeHours = officeHours;

        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getOfficeHours() {
        return officeHours;
    }

    public void setOfficeHours(String officeHours) {
        this.officeHours = officeHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @JsonProperty("isActive")
    public boolean isActive() {
        return user != null ? user.isActive() : true;
    }
    
    // Additional getter for Jackson compatibility
    public boolean getIsActive() {
        return user != null ? user.isActive() : true;
    }
}