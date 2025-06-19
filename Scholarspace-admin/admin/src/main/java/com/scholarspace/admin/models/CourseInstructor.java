package com.scholarspace.admin.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_instructors")
public class CourseInstructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;
    
    @Enumerated(EnumType.STRING)
    private InstructorRole role;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Default constructor
    public CourseInstructor() {
        this.role = InstructorRole.PRIMARY;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with fields
    public CourseInstructor(Course course, Instructor instructor, InstructorRole role) {
        this.course = course;
        this.instructor = instructor;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public InstructorRole getRole() {
        return role;
    }

    public void setRole(InstructorRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}