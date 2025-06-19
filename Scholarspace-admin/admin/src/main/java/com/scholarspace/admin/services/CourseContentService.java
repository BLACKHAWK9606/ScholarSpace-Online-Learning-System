package com.scholarspace.admin.services;

import com.scholarspace.admin.models.*;
import com.scholarspace.admin.repositories.CourseContentRepository;
import com.scholarspace.admin.repositories.CourseRepository;
import com.scholarspace.admin.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseContentService {

    private final CourseContentRepository courseContentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseContentService(CourseContentRepository courseContentRepository,
                               CourseRepository courseRepository,
                               UserRepository userRepository) {
        this.courseContentRepository = courseContentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public CourseContent createContent(Long courseId, String title, String description, 
                                     ContentType contentType, String filePath, 
                                     String fileType, Long createdById) {
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CourseContent content = new CourseContent();
        content.setCourse(course);
        content.setTitle(title);
        content.setDescription(description);
        content.setContentType(contentType);
        content.setFilePath(filePath);
        content.setFileType(fileType);
        content.setCreatedBy(createdBy);
        content.setPublishedAt(LocalDateTime.now());
        content.setCreatedAt(LocalDateTime.now());
        
        return courseContentRepository.save(content);
    }

    public CourseContent createAssignment(Long courseId, String title, String description, 
                                        String filePath, String fileType, 
                                        LocalDateTime dueDate, Long createdById) {
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CourseContent assignment = new CourseContent();
        assignment.setCourse(course);
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setContentType(ContentType.ASSIGNMENT);
        assignment.setFilePath(filePath);
        assignment.setFileType(fileType);
        assignment.setDueDate(dueDate);
        assignment.setCreatedBy(createdBy);
        assignment.setPublishedAt(LocalDateTime.now());
        assignment.setCreatedAt(LocalDateTime.now());
        
        return courseContentRepository.save(assignment);
    }

    public Optional<CourseContent> getContentById(Long id) {
        return courseContentRepository.findById(id);
    }

    public List<CourseContent> getContentByCourse(Long courseId) {
        return courseContentRepository.findByCourse_Id(courseId);
    }

    public List<CourseContent> getAssignmentsByCourse(Long courseId) {
        return courseContentRepository.findByCourse_IdAndContentType(courseId, ContentType.ASSIGNMENT);
    }

    public List<CourseContent> getLecturesByCourse(Long courseId) {
        return courseContentRepository.findByCourse_IdAndContentType(courseId, ContentType.LECTURE);
    }

    public List<CourseContent> getResourcesByCourse(Long courseId) {
        return courseContentRepository.findByCourse_IdAndContentType(courseId, ContentType.RESOURCE);
    }

    public List<CourseContent> getContentCreatedByUser(Long userId) {
        return courseContentRepository.findByCreatedBy_UserId(userId);
    }

    public CourseContent updateContent(CourseContent content) {
        return courseContentRepository.save(content);
    }

    public void deleteContent(Long contentId) {
        courseContentRepository.deleteById(contentId);
    }
}