package com.scholarspace.admin.config;

import com.scholarspace.admin.models.*;
import com.scholarspace.admin.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    @Profile("!test") // Don't run when testing
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            PasswordEncoder passwordEncoder) {

            return args -> {
            // Only initialize if no users exist
            if (userRepository.count() == 0) {
                // Create Admin user
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
                
                // Create a test instructor
                User instructor = new User();
                instructor.setName("Test Instructor");
                instructor.setEmail("instructor@example.com");
                instructor.setPassword(passwordEncoder.encode("instructor123"));
                instructor.setRole(Role.INSTRUCTOR);
                instructor.setActive(true);
                instructor.setCreatedAt(LocalDateTime.now());
                userRepository.save(instructor);
                
                // Create a test student
                User student = new User();
                student.setName("Test Student");
                student.setEmail("student@example.com");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole(Role.STUDENT);
                student.setActive(true);
                student.setCreatedAt(LocalDateTime.now());
                userRepository.save(student);
                
                // Create a test institution
                Institution institution = new Institution();
                institution.setName("Test University");
                institution.setLocation("Test Location");
                institution.setContact("contact@testuniversity.edu");
                institution.setCreatedAt(LocalDateTime.now());
                institutionRepository.save(institution);
                
                // Create a test department
                Department department = new Department();
                department.setName("Computer Science");
                department.setCode("CS");
                department.setDescription("Department of Computer Science and Information Technology");
                department.setInstitution(institution);
                department.setCreatedAt(LocalDateTime.now());
                departmentRepository.save(department);
                
                // Create a test course
                Course course = new Course();
                course.setCourseCode("CS101");
                course.setTitle("Introduction to Programming");
                course.setDescription("A beginner-friendly course on programming fundamentals.");
                course.setCreditHours(3);
                course.setSemester("Fall");
                course.setAcademicYear("2024-2025");
                course.setDepartment(department);
                course.setActive(true);
                course.setCreatedAt(LocalDateTime.now());
                courseRepository.save(course);
            }
        };
    }
}