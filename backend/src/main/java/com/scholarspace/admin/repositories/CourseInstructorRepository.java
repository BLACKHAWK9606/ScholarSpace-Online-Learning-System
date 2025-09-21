package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.CourseInstructor;
import com.scholarspace.admin.models.InstructorRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, Long> {
    List<CourseInstructor> findByCourse_Id(Long courseId);
    List<CourseInstructor> findByInstructor_InstructorId(Long instructorId);
    Optional<CourseInstructor> findByCourse_IdAndInstructor_InstructorId(Long courseId, Long instructorId);
    List<CourseInstructor> findByCourse_IdAndRole(Long courseId, InstructorRole role);
}
