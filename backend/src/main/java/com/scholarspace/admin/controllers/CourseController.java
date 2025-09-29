package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.repositories.CourseRepository;
import com.scholarspace.admin.services.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseRepository courseRepo;
    private final CourseService courseService;

    public CourseController(CourseRepository courseRepo, CourseService courseService) {
        this.courseRepo = courseRepo;
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> courseData) {
        try {
            String code = (String) courseData.get("code");
            String title = (String) courseData.get("title");
            String description = (String) courseData.get("description");
            Object creditHoursObj = courseData.get("creditHours");
            Integer creditHours = creditHoursObj instanceof Integer ? (Integer) creditHoursObj : 
                                 creditHoursObj != null ? Integer.parseInt(creditHoursObj.toString()) : null;
            // Default to empty string for optional fields
            String semester = "";  // Default value
            if (courseData.containsKey("semester") && courseData.get("semester") != null) {
                semester = courseData.get("semester").toString();
            }
            // Default to empty string for optional fields
            String academicYear = "";  // Default value
            if (courseData.containsKey("academicYear") && courseData.get("academicYear") != null) {
                academicYear = courseData.get("academicYear").toString();
            }
            Long departmentId = ((Number) courseData.get("departmentId")).longValue();
            
            // Handle isActive field
            boolean isActive = true; // Default to active
            if (courseData.containsKey("isActive") && courseData.get("isActive") != null) {
                Object activeObj = courseData.get("isActive");
                isActive = activeObj instanceof Boolean ? (Boolean) activeObj : 
                          Boolean.parseBoolean(activeObj.toString());
            }
            
            Course course = courseService.createCourse(
                code, title, description, creditHours, 
                semester, academicYear, departmentId
            );
            
            // Set the active status
            course.setActive(isActive);
            courseRepo.save(course);
            
            return ResponseEntity.ok(course);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format: " + e.getMessage()));
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required field: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Course>> getActiveCourses() {
        List<Course> allCourses = courseRepo.findAll();
        List<Course> activeCourses = allCourses.stream()
            .filter(Course::isActive)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(activeCourses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseRepo.findById(id);
        return course.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getCourseByCode(@PathVariable String code) {
        // implement getCourseByCode logic using courseRepo
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(@PathVariable Long departmentId) {
        // implement getCoursesByDepartment logic using courseRepo
        return ResponseEntity.ok(courseRepo.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        Optional<Course> existingCourse = courseRepo.findById(id);

        if (existingCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = existingCourse.get();

        if (courseDetails.getCode() != null && !courseDetails.getCode().equals(course.getCode())) {
            // Check if new course code already exists
            Optional<Course> existingByCode = courseRepo.findByCode(courseDetails.getCode());
            if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Course code already exists"));
            }
            course.setCode(courseDetails.getCode());
        }
        if (courseDetails.getTitle() != null) {
            course.setTitle(courseDetails.getTitle());
        }
        if (courseDetails.getDescription() != null) {
            course.setDescription(courseDetails.getDescription());
        }
        if (courseDetails.getCreditHours() != null) {
            course.setCreditHours(courseDetails.getCreditHours());
        }
        if (courseDetails.getSemester() != null) {
            course.setSemester(courseDetails.getSemester());
        }
        if (courseDetails.getAcademicYear() != null) {
            course.setAcademicYear(courseDetails.getAcademicYear());
        }
        // Update the active status - handle both boolean and null cases
        if (courseDetails.isActive() != course.isActive()) {
            course.setActive(courseDetails.isActive());
        }

        Course updatedCourse = courseRepo.save(course);
        return ResponseEntity.ok(updatedCourse);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateCourse(@PathVariable Long id) {
        Optional<Course> courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Course course = courseOpt.get();
        course.setActive(true);
        courseRepo.save(course);
        
        return ResponseEntity.ok(Map.of("message", "Course activated successfully"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCourse(@PathVariable Long id) {
        Optional<Course> courseOpt = courseRepo.findById(id);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Course course = courseOpt.get();
        course.setActive(false);
        courseRepo.save(course);
        
        return ResponseEntity.ok(Map.of("message", "Course deactivated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            if (!courseRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            courseService.deleteCourse(id);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}