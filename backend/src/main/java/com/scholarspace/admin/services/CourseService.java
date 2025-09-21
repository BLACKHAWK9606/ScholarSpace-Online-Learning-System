package com.scholarspace.admin.services;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.Department;
import com.scholarspace.admin.repositories.CourseRepository;
import com.scholarspace.admin.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public CourseService(CourseRepository courseRepository, DepartmentRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Get a course by ID
     * 
     * @param id The course ID
     * @return The course
     */
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Course createCourse(String courseCode, String title, String description,
                              Integer creditHours, String semester, String academicYear,
                              Long departmentId) {
        
        // Check if course code is already used
        if (courseRepository.findByCode(courseCode).isPresent()) {
            throw new RuntimeException("Course code already exists");
        }
        
        // Find department
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setTitle(title);
        course.setDescription(description);
        course.setCreditHours(creditHours);
        course.setSemester(semester);
        course.setAcademicYear(academicYear);
        course.setDepartment(department);
        course.setActive(true);
        course.setCreatedAt(LocalDateTime.now());
        
        return courseRepository.save(course);
    }

    public Optional<Course> getCourseByCode(String courseCode) {
        return courseRepository.findByCode(courseCode);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findByIsActiveTrue();
    }

    public List<Course> getCoursesByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentDepartmentId(departmentId);
    }

    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }

    public void deactivateCourse(Long courseId) {
        courseRepository.findById(courseId).ifPresent(course -> {
            course.setActive(false);
            courseRepository.save(course);
        });
    }

    public void activateCourse(Long courseId) {
        courseRepository.findById(courseId).ifPresent(course -> {
            course.setActive(true);
            courseRepository.save(course);
        });
    }
}