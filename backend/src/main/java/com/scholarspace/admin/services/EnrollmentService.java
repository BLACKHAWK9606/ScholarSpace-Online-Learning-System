package com.scholarspace.admin.services;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.Enrollment;
import com.scholarspace.admin.models.EnrollmentStatus;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.repositories.CourseRepository;
import com.scholarspace.admin.repositories.EnrollmentRepository;
import com.scholarspace.admin.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public Enrollment requestEnrollment(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Check if student is already enrolled
        Optional<Enrollment> existingEnrollment = 
                enrollmentRepository.findByCourse_IdAndStudent_UserId(courseId, studentId);
        
        if (existingEnrollment.isPresent()) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.PENDING); // Changed from ACTIVE to PENDING for admin approval
        enrollment.setCreatedAt(LocalDateTime.now());
        
        return enrollmentRepository.save(enrollment);
    }

    public Optional<Enrollment> getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id);
    }

    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourse_Id(courseId);
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudent_UserId(studentId);
    }

    public List<Enrollment> getActiveEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourse_IdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudent_UserIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
    }

    public List<Enrollment> getEnrollmentsByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }

    public Enrollment updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    public void dropEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
    }

    public void completeEnrollment(Long enrollmentId, String grade) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setGrade(grade);
        enrollmentRepository.save(enrollment);
    }
}
