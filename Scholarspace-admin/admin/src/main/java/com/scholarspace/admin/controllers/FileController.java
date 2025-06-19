package com.scholarspace.admin.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.FileStorage;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.services.CourseService;
import com.scholarspace.admin.services.FileStorageService;
import com.scholarspace.admin.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final CourseService courseService;

    public FileController(FileStorageService fileStorageService, 
                         UserService userService,
                         CourseService courseService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.courseService = courseService;
    }

    /**
     * Upload a file
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") FileStorage.FileType fileType,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            Authentication authentication) {
        
        try {
            // Get current user
            User currentUser = userService.getCurrentUser(authentication);
            
            // Get course if courseId is provided
            Course course = null;
            if (courseId != null) {
                Optional<Course> optionalCourse = courseService.getCourseById(courseId);
                if (!optionalCourse.isPresent()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Course not found with ID: " + courseId));
                }
                course = optionalCourse.get();
            }
            
            // Store the file
            FileStorage storedFile = fileStorageService.storeFile(file, currentUser, fileType, course, isPublic);
            
            // Create download URL
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/download/")
                    .path(storedFile.getFileName())
                    .toUriString();
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", storedFile.getId());
            response.put("fileName", storedFile.getFileName());
            response.put("originalFileName", storedFile.getOriginalFileName());
            response.put("contentType", storedFile.getContentType());
            response.put("fileSize", storedFile.getFileSize());
            response.put("uploadDate", storedFile.getUploadDate());
            response.put("fileType", storedFile.getFileType());
            response.put("downloadUrl", fileDownloadUri);
            response.put("isPublic", storedFile.isPublic());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload file: " + ex.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body((Resource) new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            
            // Get file metadata
            FileStorage fileMetadata = fileStorageService.getFileByFileName(fileName)
                    .orElseThrow(() -> new RuntimeException("File metadata not found for: " + fileName));

            // Try to determine file's content type
            String contentType = fileMetadata.getContentType();
            
            // Fallback to the default content type if type could not be determined
            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to download file: " + ex.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body((Resource) new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Get files for a course
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getFilesByCourse(
            @PathVariable Long courseId,
            @RequestParam(value = "fileType", required = false) FileStorage.FileType fileType) {
        
        try {
            Optional<Course> optionalCourse = courseService.getCourseById(courseId);
            if (!optionalCourse.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Course not found with ID: " + courseId));
            }
            
            Course course = optionalCourse.get();
            List<FileStorage> files;
            if (fileType != null) {
                files = fileStorageService.getFilesByCourseAndType(course, fileType);
            } else {
                files = fileStorageService.getFilesByCourse(course);
            }
            
            List<Map<String, Object>> response = files.stream().map(file -> {
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/files/download/")
                        .path(file.getFileName())
                        .toUriString();
                
                Map<String, Object> fileResponse = new HashMap<>();
                fileResponse.put("id", file.getId());
                fileResponse.put("originalFileName", file.getOriginalFileName());
                fileResponse.put("contentType", file.getContentType());
                fileResponse.put("fileSize", file.getFileSize());
                fileResponse.put("uploadDate", file.getUploadDate());
                fileResponse.put("fileType", file.getFileType());
                fileResponse.put("uploadedBy", file.getUploadedBy().getName());
                fileResponse.put("isPublic", file.isPublic());
                fileResponse.put("downloadUrl", fileDownloadUri);
                
                return fileResponse;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve files: " + ex.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body((Resource) new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Get files uploaded by current user
     */
    @GetMapping("/my-files")
    public ResponseEntity<?> getMyFiles(Authentication authentication) {
        try {
            User currentUser = userService.getCurrentUser(authentication);
            
            List<FileStorage> files = fileStorageService.getFilesByUser(currentUser);
            
            List<Map<String, Object>> response = files.stream().map(file -> {
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/files/download/")
                        .path(file.getFileName())
                        .toUriString();
                
                Map<String, Object> fileResponse = new HashMap<>();
                fileResponse.put("id", file.getId());
                fileResponse.put("originalFileName", file.getOriginalFileName());
                fileResponse.put("contentType", file.getContentType());
                fileResponse.put("fileSize", file.getFileSize());
                fileResponse.put("uploadDate", file.getUploadDate());
                fileResponse.put("fileType", file.getFileType());
                fileResponse.put("isPublic", file.isPublic());
                fileResponse.put("downloadUrl", fileDownloadUri);
                
                return fileResponse;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve your files: " + ex.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body((Resource) new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Delete a file
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id, Authentication authentication) {
        try {
            // Get current user
            User currentUser = userService.getCurrentUser(authentication);
            
            // Get file metadata
            Optional<FileStorage> optionalFile = fileStorageService.getFileById(id);
            if (!optionalFile.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "File not found with ID: " + id));
            }
            
            FileStorage file = optionalFile.get();
            
            // Check if user has permission to delete the file
            if (!file.getUploadedBy().getUserId().equals(currentUser.getUserId()) && 
                !currentUser.getRole().toString().equals("ADMIN")) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "You don't have permission to delete this file"));
            }
            
            // Delete the file
            fileStorageService.deleteFile(id);
            
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
            
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete file: " + ex.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body((Resource) new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Update file metadata
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFileMetadata(
            @PathVariable Long id,
            @RequestParam("fileType") FileStorage.FileType fileType,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            Authentication authentication) {
        
        try {
            // Get current user
            User currentUser = userService.getCurrentUser(authentication);
            
            // Get file metadata
            Optional<FileStorage> optionalFile = fileStorageService.getFileById(id);
            if (!optionalFile.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "File not found with ID: " + id));
            }
            
            FileStorage file = optionalFile.get();
            
            // Check if user has permission to update the file
            if (!file.getUploadedBy().getUserId().equals(currentUser.getUserId()) && 
                !currentUser.getRole().toString().equals("ADMIN")) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "You don't have permission to update this file"));
            }
            
            // Update file metadata
            FileStorage updatedFile = fileStorageService.updateFileMetadata(id, fileType, isPublic);
            
            return ResponseEntity.ok(updatedFile);
            
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update file metadata: " + ex.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body((Resource) new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
