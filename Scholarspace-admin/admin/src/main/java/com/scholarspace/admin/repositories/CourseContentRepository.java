package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.CourseContent;
import com.scholarspace.admin.models.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    // Updated methods to use course_id instead of course_courseId
    List<CourseContent> findByCourse_Id(Long courseId);
    List<CourseContent> findByCourse_IdAndContentType(Long courseId, ContentType contentType);
    
    // Change this method to use createdBy_userId
    List<CourseContent> findByCreatedBy_UserId(Long userId);
}