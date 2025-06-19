package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Submission;
import com.scholarspace.admin.services.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final String uploadDir = "uploads/submissions/";

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
        
        // Create upload directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @PostMapping("/upload")
public ResponseEntity<?> submitAssignment(
        @RequestParam("file") MultipartFile file,
        @RequestParam("assignmentId") Long assignmentId,
        @RequestParam("studentId") Long studentId) {
    
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
        
        // Create submission
        Submission submission = submissionService.submitAssignment(
            assignmentId, studentId, filePath.toString());
        
        return ResponseEntity.ok(submission);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

    @PostMapping("/{id}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long id,
            @RequestBody Map<String, Object> gradeRequest) {
        
        try {
            Double grade = Double.valueOf(gradeRequest.get("grade").toString());
            String feedback = (String) gradeRequest.get("feedback");
            Long gradedById = Long.valueOf(gradeRequest.get("gradedById").toString());
            
            Submission submission = submissionService.gradeSubmission(id, grade, feedback, gradedById);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubmissionById(@PathVariable Long id) {
        Optional<Submission> submission = submissionService.getSubmissionById(id);
        return submission.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignment(assignmentId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByStudent(studentId));
    }

    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<?> getSubmissionByAssignmentAndStudent(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        
        Optional<Submission> submission = 
                submissionService.getSubmissionByAssignmentAndStudent(assignmentId, studentId);
        return submission.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ungraded")
    public ResponseEntity<List<Submission>> getUngradedSubmissions() {
        return ResponseEntity.ok(submissionService.getUngradedSubmissions());
    }

    @GetMapping("/graded")
    public ResponseEntity<List<Submission>> getGradedSubmissions() {
        return ResponseEntity.ok(submissionService.getGradedSubmissions());
    }
}