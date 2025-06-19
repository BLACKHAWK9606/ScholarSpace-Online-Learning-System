package com.scholarspace.admin.services;

import com.scholarspace.admin.models.*;
import com.scholarspace.admin.repositories.CourseInstructorRepository;
import com.scholarspace.admin.repositories.DepartmentRepository;
import com.scholarspace.admin.repositories.InstructorRepository;
import com.scholarspace.admin.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;

    public InstructorService(InstructorRepository instructorRepository, 
                             UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             CourseInstructorRepository courseInstructorRepository) {
        this.instructorRepository = instructorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
    }

    public Instructor createInstructor(Long userId, Long departmentId, 
                                      String specialization, String officeLocation, 
                                      String officeHours) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new RuntimeException("User must have INSTRUCTOR role");
        }
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        if (instructorRepository.findByUser(user).isPresent()) {
            throw new RuntimeException("User is already registered as an instructor");
        }
        
        Instructor instructor = new Instructor();
        instructor.setUser(user);
        instructor.setDepartment(department);
        instructor.setSpecialization(specialization);
        instructor.setOfficeLocation(officeLocation);
        instructor.setOfficeHours(officeHours);
        instructor.setCreatedAt(LocalDateTime.now());
        
        return instructorRepository.save(instructor);
    }

    public Optional<Instructor> getInstructorById(Long id) {
        return instructorRepository.findById(id);
    }

    public Optional<Instructor> getInstructorByUserId(Long userId) {
        return instructorRepository.findByUserUserId(userId);
    }

    public List<Instructor> getAllInstructors() {
        return instructorRepository.findAll();
    }

    public Instructor updateInstructor(Instructor instructor) {
        return instructorRepository.save(instructor);
    }
    
    public Instructor updateInstructorDepartment(Long instructorId, Long departmentId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        instructor.setDepartment(department);
        return instructorRepository.save(instructor);
    }

    public void assignInstructorToCourse(Long instructorId, Long courseId, InstructorRole role) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        // Check if this instructor is already assigned to this course
        Optional<CourseInstructor> existingAssignment = 
                courseInstructorRepository.findByCourse_IdAndInstructor_InstructorId(courseId, instructorId);
        
        if (existingAssignment.isPresent()) {
            // Update the role if it's different
            CourseInstructor assignment = existingAssignment.get();
            if (assignment.getRole() != role) {
                assignment.setRole(role);
                courseInstructorRepository.save(assignment);
            }
        } else {
            // Create new assignment
            CourseInstructor courseInstructor = new CourseInstructor();
            Course course = new Course();
            course.setCourseId(courseId);

            courseInstructor.setCourse(course);
            courseInstructor.setInstructor(instructor);
            courseInstructor.setRole(role);
            courseInstructor.setCreatedAt(LocalDateTime.now());
            
            courseInstructorRepository.save(courseInstructor);
        }
    }

    public void removeInstructorFromCourse(Long instructorId, Long courseId) {
        courseInstructorRepository.findByCourse_IdAndInstructor_InstructorId(courseId, instructorId)
                .ifPresent(courseInstructorRepository::delete);
    }

    public List<CourseInstructor> getCoursesByInstructor(Long instructorId) {
        return courseInstructorRepository.findByInstructor_InstructorId(instructorId);
    }

    public List<CourseInstructor> getInstructorsByCourse(Long courseId) {
        return courseInstructorRepository.findByCourse_Id(courseId);
    }
}
