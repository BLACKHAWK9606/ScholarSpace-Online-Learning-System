package com.scholarspace.admin.services;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.Department;
import com.scholarspace.admin.repositories.CourseRepository;
import com.scholarspace.admin.repositories.DepartmentRepository;
import com.scholarspace.admin.repositories.EnrollmentRepository;
import com.scholarspace.admin.repositories.CourseInstructorRepository;
import com.scholarspace.admin.repositories.CourseContentRepository;
import com.scholarspace.admin.repositories.SubmissionRepository;
import com.scholarspace.admin.repositories.FileStorageRepository;
import com.scholarspace.admin.repositories.AttendanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseContentRepository courseContentRepository;
    private final SubmissionRepository submissionRepository;
    private final FileStorageRepository fileStorageRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public CourseService(CourseRepository courseRepository, DepartmentRepository departmentRepository,
                        EnrollmentRepository enrollmentRepository, CourseInstructorRepository courseInstructorRepository,
                        CourseContentRepository courseContentRepository, SubmissionRepository submissionRepository,
                        FileStorageRepository fileStorageRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.courseContentRepository = courseContentRepository;
        this.submissionRepository = submissionRepository;
        this.fileStorageRepository = fileStorageRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
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

    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found");
        }
        
        Course course = courseRepository.findById(courseId).get();
        
        // Delete submissions for course contents first
        courseContentRepository.findByCourse_Id(courseId).forEach(content -> {
            submissionRepository.deleteAll(submissionRepository.findByAssignment_ContentId(content.getContentId()));
        });
        
        // Delete all course contents for this course
        courseContentRepository.deleteAll(courseContentRepository.findByCourse_Id(courseId));
        
        // Delete all file storage entries for this course
        fileStorageRepository.deleteAll(fileStorageRepository.findByCourse(course));
        
        // Delete all attendance records for this course
        attendanceRecordRepository.deleteAll(attendanceRecordRepository.findByCourse_Id(courseId));
        
        // Delete all enrollments for this course
        enrollmentRepository.deleteAll(enrollmentRepository.findByCourse_Id(courseId));
        
        // Delete all instructor assignments for this course
        courseInstructorRepository.deleteAll(courseInstructorRepository.findByCourse_Id(courseId));
        
        // Remove this course as a prerequisite from other courses using efficient query
        courseRepository.removeAsPrerequisite(courseId);
        
        // Finally delete the course
        courseRepository.deleteById(courseId);
    }
}