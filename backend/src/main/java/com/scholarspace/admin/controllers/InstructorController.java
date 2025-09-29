package com.scholarspace.admin.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scholarspace.admin.models.CourseInstructor;
import com.scholarspace.admin.models.Instructor;
import com.scholarspace.admin.models.InstructorRole;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.services.InstructorService;

@RestController
@RequestMapping("/api/instructors")
public class InstructorController {
    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @PostMapping
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<?> createInstructor(@RequestBody Map<String, Object> instructorRequest) {
        try {
            Long userId = Long.valueOf(instructorRequest.get("userId").toString());
            Long departmentId = Long.valueOf(instructorRequest.get("departmentId").toString());
            String specialization = (String) instructorRequest.get("specialization");
            String officeLocation = (String) instructorRequest.get("officeLocation");
            String officeHours = (String) instructorRequest.get("officeHours");
            
            Instructor instructor = instructorService.createInstructor(userId, departmentId, 
                                                                    specialization, officeLocation, 
                                                                    officeHours);
            return ResponseEntity.ok(instructor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Instructor>> getAllInstructors() {
        return ResponseEntity.ok(instructorService.getAllInstructors());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Instructor>> getActiveInstructors() {
        return ResponseEntity.ok(instructorService.getActiveInstructors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInstructorById(@PathVariable Long id) {
        Optional<Instructor> instructor = instructorService.getInstructorById(id);
        return instructor.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getInstructorByUserId(@PathVariable Long userId) {
        Optional<Instructor> instructor = instructorService.getInstructorByUserId(userId);
        return instructor.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{instructorId}/courses/{courseId}/validate")
    public ResponseEntity<?> validateInstructorCourseAssignment(
            @PathVariable Long instructorId, 
            @PathVariable Long courseId) {
        try {
            InstructorService.ValidationResult validation = instructorService.validateInstructorCourseAssignment(instructorId, courseId);
            
            if (validation.isValid()) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", validation.getMessage(),
                    "instructorId", instructorId,
                    "courseId", courseId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", validation.getMessage(),
                    "instructorId", instructorId,
                    "courseId", courseId
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "valid", false,
                "error", "Server error during validation: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/{instructorId}/courses/{courseId}")
    public ResponseEntity<?> assignInstructorToCourse(
            @PathVariable Long instructorId, 
            @PathVariable Long courseId,
            @RequestBody(required = false) Map<String, String> request) {
        
        System.out.println("Assigning instructor " + instructorId + " to course " + courseId);
        if (request != null) {
            System.out.println("Request body: " + request);
        } else {
            System.out.println("No request body provided");
        }
        
        try {
            // Default to PRIMARY role if not specified
            InstructorRole role = InstructorRole.PRIMARY;
            if (request != null && request.containsKey("role")) {
                String roleStr = request.get("role").toUpperCase();
                System.out.println("Role from request: " + roleStr);
                role = InstructorRole.valueOf(roleStr);
            } else {
                System.out.println("Using default role: PRIMARY");
            }
            
            // Validate assignment before attempting
            InstructorService.ValidationResult validation = instructorService.validateInstructorCourseAssignment(instructorId, courseId);
            if (!validation.isValid()) {
                System.out.println("Assignment validation failed: " + validation.getMessage());
                return ResponseEntity.badRequest().body(Map.of("error", validation.getMessage()));
            }
            
            // Assign the instructor to the course
            instructorService.assignInstructorToCourse(instructorId, courseId, role);
            System.out.println("Instructor successfully assigned to course");
            
            return ResponseEntity.ok(Map.of(
                "message", "Instructor assigned to course successfully",
                "instructorId", instructorId,
                "courseId", courseId,
                "role", role.toString()
            ));
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{instructorId}/courses/{courseId}")
    public ResponseEntity<?> removeInstructorFromCourse(
            @PathVariable Long instructorId, 
            @PathVariable Long courseId) {
        try {
            instructorService.removeInstructorFromCourse(instructorId, courseId);
            return ResponseEntity.ok(Map.of("message", "Instructor removed from course successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @GetMapping("/{instructorId}/courses")
    public ResponseEntity<List<CourseInstructor>> getCoursesByInstructor(@PathVariable Long instructorId) {
        return ResponseEntity.ok(instructorService.getCoursesByInstructor(instructorId));
    }
    
    @GetMapping("/{instructorId}/courses/detailed")
    public ResponseEntity<?> getDetailedCoursesByInstructor(@PathVariable Long instructorId) {
        try {
            List<Map<String, Object>> detailedCourses = instructorService.getDetailedCoursesByInstructor(instructorId);
            return ResponseEntity.ok(detailedCourses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<CourseInstructor>> getInstructorsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(instructorService.getInstructorsByCourse(courseId));
    }
    
    @PutMapping("/{id}")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<?> updateInstructor(
            @PathVariable Long id,
            @RequestBody Map<String, Object> instructorRequest) {
        try {
            Optional<Instructor> existingInstructor = instructorService.getInstructorById(id);
            if (existingInstructor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Instructor instructor = existingInstructor.get();
            
            // Update user information if provided
            User user = instructor.getUser();
            if (user != null) {
                if (instructorRequest.containsKey("name")) {
                    user.setName((String) instructorRequest.get("name"));
                }
                if (instructorRequest.containsKey("email")) {
                    user.setEmail((String) instructorRequest.get("email"));
                }
                if (instructorRequest.containsKey("isActive")) {
                    Object activeValue = instructorRequest.get("isActive");
                    if (activeValue != null) {
                        boolean isActive = activeValue instanceof Boolean ? (Boolean) activeValue : Boolean.parseBoolean(activeValue.toString());
                        
                        if (isActive) {
                            instructorService.activateInstructor(id);
                        } else {
                            instructorService.deactivateInstructor(id);
                        }
                        
                        instructor = instructorService.getInstructorById(id).orElse(instructor);
                    }
                }
            }
            
            // Update department if provided
            if (instructorRequest.containsKey("departmentId")) {
                Long departmentId = Long.valueOf(instructorRequest.get("departmentId").toString());
                instructor = instructorService.updateInstructorDepartment(id, departmentId);
            }
            
            // Update specialization if provided
            if (instructorRequest.containsKey("specialization")) {
                String specialization = (String) instructorRequest.get("specialization");
                instructor.setSpecialization(specialization);
            }
            
            // Update office location if provided
            if (instructorRequest.containsKey("officeLocation")) {
                String officeLocation = (String) instructorRequest.get("officeLocation");
                instructor.setOfficeLocation(officeLocation);
            }
            
            // Update office hours if provided
            if (instructorRequest.containsKey("officeHours")) {
                String officeHours = (String) instructorRequest.get("officeHours");
                instructor.setOfficeHours(officeHours);
            }
            
            // Only update if we haven't already updated via activate/deactivate
            if (!instructorRequest.containsKey("isActive")) {
                instructor = instructorService.updateInstructor(instructor);
            }
            return ResponseEntity.ok(instructor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateInstructor(@PathVariable Long id) {
        try {
            instructorService.activateInstructor(id);
            return ResponseEntity.ok(Map.of("message", "Instructor activated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateInstructor(@PathVariable Long id) {
        try {
            instructorService.deactivateInstructor(id);
            return ResponseEntity.ok(Map.of("message", "Instructor deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}