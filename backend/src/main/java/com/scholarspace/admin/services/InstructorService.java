package com.scholarspace.admin.services;

import com.scholarspace.admin.models.*;
import com.scholarspace.admin.repositories.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public InstructorService(InstructorRepository instructorRepository, 
                             UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             CourseInstructorRepository courseInstructorRepository,
                             EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository) {
        this.instructorRepository = instructorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
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
        
        Instructor savedInstructor = instructorRepository.save(instructor);
        
        // Ensure user is also saved with any potential changes
        userRepository.save(user);
        
        return savedInstructor;
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
    
    public List<Instructor> getActiveInstructors() {
        return instructorRepository.findByActiveTrue();
    }

    public Instructor updateInstructor(Instructor instructor) {
        // Also save the associated user to ensure changes are persisted
        if (instructor.getUser() != null) {
            userRepository.save(instructor.getUser());
        }
        return instructorRepository.save(instructor);
    }
    
    public void activateInstructor(Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        // Update associated user active status
        User user = instructor.getUser();
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
        }
    }
    
    public void deactivateInstructor(Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        // Update associated user active status
        User user = instructor.getUser();
        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
        }
    }
    
    public Instructor updateInstructorDepartment(Long instructorId, Long departmentId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        instructor.setDepartment(department);
        return instructorRepository.save(instructor);
    }

    /**
     * Validates if an instructor can be assigned to a course
     * @param instructorId The instructor ID
     * @param courseId The course ID
     * @return ValidationResult containing success status and detailed message
     */
    public ValidationResult validateInstructorCourseAssignment(Long instructorId, Long courseId) {
        // Check if instructor exists
        Optional<Instructor> instructorOpt = instructorRepository.findById(instructorId);
        if (instructorOpt.isEmpty()) {
            return new ValidationResult(false, "Validation failed: Instructor with ID " + instructorId + " not found");
        }
        
        // Check if course exists
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return new ValidationResult(false, "Validation failed: Course with ID " + courseId + " not found");
        }
        
        Instructor instructor = instructorOpt.get();
        Course course = courseOpt.get();
        
        // Check if instructor is active
        if (!instructor.isActive()) {
            return new ValidationResult(false, "Validation failed: Cannot assign inactive instructor '" + 
                instructor.getUser().getName() + "' (ID: " + instructorId + ") to course. " +
                "Reason: Instructor account is currently deactivated. Please activate the instructor account before assignment.");
        }
        
        // Check if course is active
        if (!course.isActive()) {
            return new ValidationResult(false, "Validation failed: Cannot assign instructor to inactive course '" + 
                course.getTitle() + "' (" + course.getCode() + "). " +
                "Reason: Course is currently deactivated. Please activate the course before assignment.");
        }
        
        return new ValidationResult(true, "Assignment validation successful: Instructor '" + 
            instructor.getUser().getName() + "' can be assigned to course '" + course.getTitle() + "' (" + course.getCode() + ")");
    }
    
    public void assignInstructorToCourse(Long instructorId, Long courseId, InstructorRole role) {
        // Validate instructor exists
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Assignment failed: Instructor with ID " + instructorId + " not found"));
        
        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Assignment failed: Course with ID " + courseId + " not found"));
        
        // Validate instructor is active
        if (!instructor.isActive()) {
            throw new RuntimeException("Assignment failed: Cannot assign inactive instructor '" + 
                instructor.getUser().getName() + "' (ID: " + instructorId + ") to course. " +
                "Reason: Instructor account is currently deactivated. Please activate the instructor account before assignment.");
        }
        
        // Validate course is active
        if (!course.isActive()) {
            throw new RuntimeException("Assignment failed: Cannot assign instructor to inactive course '" + 
                course.getTitle() + "' (" + course.getCode() + "). " +
                "Reason: Course is currently deactivated. Please activate the course before assignment.");
        }
        
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
    
    public List<Map<String, Object>> getDetailedCoursesByInstructor(Long instructorId) {
        List<CourseInstructor> courseInstructors = courseInstructorRepository.findByInstructor_InstructorId(instructorId);
        
        return courseInstructors.stream().map(ci -> {
            Course course = ci.getCourse();
            
            Map<String, Object> courseDetails = new HashMap<>();
            courseDetails.put("id", course.getId());
            courseDetails.put("courseId", course.getId());
            courseDetails.put("code", course.getCode());
            courseDetails.put("courseCode", course.getCode());
            courseDetails.put("title", course.getTitle());
            courseDetails.put("courseName", course.getTitle());
            courseDetails.put("description", course.getDescription());
            courseDetails.put("creditHours", course.getCreditHours());
            courseDetails.put("semester", course.getSemester());
            courseDetails.put("academicYear", course.getAcademicYear());
            courseDetails.put("isActive", course.isActive());
            courseDetails.put("status", course.isActive() ? "Active" : "Inactive");
            courseDetails.put("role", ci.getRole().toString());
            courseDetails.put("assignedAt", ci.getCreatedAt());
            
            if (course.getDepartment() != null) {
                courseDetails.put("department", Map.of(
                    "id", course.getDepartment().getDepartmentId(),
                    "name", course.getDepartment().getName()
                ));
            }
            
            return courseDetails;
        }).collect(Collectors.toList());
    }
    
    /**
     * Inner class to represent validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}