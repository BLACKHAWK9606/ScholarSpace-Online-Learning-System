package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    List<Course> findByDepartmentDepartmentId(Long departmentId);
    List<Course> findByIsActiveTrue();
}