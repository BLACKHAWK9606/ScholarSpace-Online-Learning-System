package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByCourse_Id(Long courseId);
    List<AttendanceRecord> findByStudent_UserId(Long studentId);
}