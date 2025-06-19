package com.scholarspace.admin.services;

import com.scholarspace.admin.models.CourseContent;
import com.scholarspace.admin.models.Submission;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.models.ContentType;
import com.scholarspace.admin.repositories.CourseContentRepository;
import com.scholarspace.admin.repositories.SubmissionRepository;
import com.scholarspace.admin.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final CourseContentRepository courseContentRepository;
    private final UserRepository userRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                            CourseContentRepository courseContentRepository,
                            UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.courseContentRepository = courseContentRepository;
        this.userRepository = userRepository;
    }

    public Submission submitAssignment(Long assignmentId, Long studentId, String filePath) {
        CourseContent assignment = courseContentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        if (assignment.getContentType() != ContentType.ASSIGNMENT) {
            throw new RuntimeException("The content is not an assignment");
        }
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Check if assignment is past due date
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Assignment submission deadline has passed");
        }
        
        // Check if student has already submitted
        Optional<Submission> existingSubmission = 
                submissionRepository.findByAssignment_ContentIdAndStudent_UserId(assignmentId, studentId);
        
        if (existingSubmission.isPresent()) {
            // Update existing submission
            Submission submission = existingSubmission.get();
            submission.setFilePath(filePath);
            submission.setSubmissionDate(LocalDateTime.now());
            return submissionRepository.save(submission);
        } else {
            // Create new submission
            Submission submission = new Submission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setFilePath(filePath);
            submission.setSubmissionDate(LocalDateTime.now());
            submission.setCreatedAt(LocalDateTime.now());
            return submissionRepository.save(submission);
        }
    }

    public Submission gradeSubmission(Long submissionId, Double grade, String feedback, Long gradedById) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        User gradedBy = userRepository.findById(gradedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        submission.setGrade(grade);
        submission.setFeedback(feedback);
        submission.setGradedBy(gradedBy);
        submission.setGradedAt(LocalDateTime.now());
        
        return submissionRepository.save(submission);
    }

    public Optional<Submission> getSubmissionById(Long id) {
        return submissionRepository.findById(id);
    }

    public List<Submission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignment_ContentId(assignmentId);
    }

    public List<Submission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudent_UserId(studentId);
    }

    public Optional<Submission> getSubmissionByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignment_ContentIdAndStudent_UserId(assignmentId, studentId);
    }

    public List<Submission> getUngradedSubmissions() {
        return submissionRepository.findByGradeIsNull();
    }

    public List<Submission> getGradedSubmissions() {
        return submissionRepository.findByGradeIsNotNull();
    }
}
