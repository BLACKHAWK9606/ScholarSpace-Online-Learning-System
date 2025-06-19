package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignment_ContentId(Long assignmentId);
    List<Submission> findByStudent_UserId(Long studentId);
    Optional<Submission> findByAssignment_ContentIdAndStudent_UserId(Long assignmentId, Long studentId);
    List<Submission> findByGradeIsNull();
    List<Submission> findByGradeIsNotNull();
}
