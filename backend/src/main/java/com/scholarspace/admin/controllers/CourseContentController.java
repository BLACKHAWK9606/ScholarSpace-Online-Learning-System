package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.ContentType;
import com.scholarspace.admin.models.CourseContent;
import com.scholarspace.admin.services.CourseContentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
public class CourseContentController {
    private final CourseContentService courseContentService;
    private final String uploadDir = "uploads/course-contents/";

    public CourseContentController(CourseContentService courseContentService) {
        this.courseContentService = courseContentService;
        
        // Create upload directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @PostMapping("/upload")
public ResponseEntity<?> uploadContent(
        @RequestParam("file") MultipartFile file,
        @RequestParam("courseId") Long courseId,
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("contentType") String contentTypeStr,
        @RequestParam("createdById") Long createdById,
        @RequestParam(value = "dueDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate) {
    
    try {
        // Save the file
        String originalFilename = file.getOriginalFilename();
        String newFilename;
        
        if (originalFilename != null && originalFilename.contains(".")) {
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            newFilename = UUID.randomUUID().toString() + fileExtension;
        } else {
            // Default extension if original filename is null or doesn't have an extension
            newFilename = UUID.randomUUID().toString();
        }
        
        Path filePath = Paths.get(uploadDir + newFilename);
        Files.write(filePath, file.getBytes());
        
        // Create course content
        ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
        String fileType = file.getContentType();
        
        CourseContent content;
        if (contentType == ContentType.ASSIGNMENT && dueDate != null) {
            content = courseContentService.createAssignment(
                courseId, title, description, filePath.toString(), fileType, dueDate, createdById);
        } else {
            content = courseContentService.createContent(
                courseId, title, description, contentType, filePath.toString(), fileType, createdById);
        }
        
        return ResponseEntity.ok(content);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<?> getContentById(@PathVariable Long id) {
        Optional<CourseContent> content = courseContentService.getContentById(id);
        return content.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseContent>> getContentByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseContentService.getContentByCourse(courseId));
    }

    @GetMapping("/course/{courseId}/assignments")
    public ResponseEntity<List<CourseContent>> getAssignmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseContentService.getAssignmentsByCourse(courseId));
    }

    @GetMapping("/course/{courseId}/lectures")
    public ResponseEntity<List<CourseContent>> getLecturesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseContentService.getLecturesByCourse(courseId));
    }

    @GetMapping("/course/{courseId}/resources")
    public ResponseEntity<List<CourseContent>> getResourcesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseContentService.getResourcesByCourse(courseId));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseContent>> getContentByInstructor(@PathVariable Long instructorId) {
        return ResponseEntity.ok(courseContentService.getContentCreatedByUser(instructorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContent(@PathVariable Long id, @RequestBody CourseContent contentDetails) {
        Optional<CourseContent> existingContent = courseContentService.getContentById(id);
        
        if (existingContent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CourseContent content = existingContent.get();
        
        if (contentDetails.getTitle() != null) {
            content.setTitle(contentDetails.getTitle());
        }
        if (contentDetails.getDescription() != null) {
            content.setDescription(contentDetails.getDescription());
        }
        if (contentDetails.getDueDate() != null) {
            content.setDueDate(contentDetails.getDueDate());
        }
        
        CourseContent updatedContent = courseContentService.updateContent(content);
        return ResponseEntity.ok(updatedContent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        try {
            // Get the content to retrieve file path
            Optional<CourseContent> contentOptional = courseContentService.getContentById(id);
            if (contentOptional.isPresent()) {
                CourseContent content = contentOptional.get();
                String filePath = content.getFilePath();
                
                // Delete the content from database
                courseContentService.deleteContent(id);
                
                // Delete the file from filesystem
                if (filePath != null && !filePath.isEmpty()) {
                    Files.deleteIfExists(Paths.get(filePath));
                }
                
                return ResponseEntity.ok(Map.of("message", "Content deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}