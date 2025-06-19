package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.Enrollment;
import com.scholarspace.admin.models.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourse_Id(Long courseId);
    List<Enrollment> findByStudent_UserId(Long studentId);
    List<Enrollment> findByCourse_IdAndStatus(Long courseId, EnrollmentStatus status);
    List<Enrollment> findByStudent_UserIdAndStatus(Long studentId, EnrollmentStatus status);
    Optional<Enrollment> findByCourse_IdAndStudent_UserId(Long courseId, Long studentId);
    List<Enrollment> findByStatus(EnrollmentStatus status);
}
