package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Submission;
import com.scholarspace.admin.services.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SubmissionService submissionService;

    public NotificationController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<?> getInstructorNotifications(@PathVariable Long instructorId) {
        try {
            // Get recent ungraded submissions as notifications
            List<Submission> ungradedSubmissions = submissionService.getUngradedSubmissions();
            
            // Convert to notification format
            List<Map<String, Object>> notifications = ungradedSubmissions.stream()
                .limit(5) // Limit to 5 most recent
                .map(submission -> {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("id", submission.getSubmissionId());
                    notification.put("type", "submission");
                    notification.put("title", "New Submission");
                    notification.put("message", "New assignment submission received");
                    notification.put("studentName", submission.getStudent() != null ? submission.getStudent().getName() : "Unknown Student");
                    notification.put("assignmentTitle", submission.getAssignment() != null ? submission.getAssignment().getTitle() : "Assignment");
                    notification.put("submittedAt", submission.getSubmissionDate().toString());
                    notification.put("isRead", false);
                    return notification;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}