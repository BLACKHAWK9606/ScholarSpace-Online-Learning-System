package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.Instructor;
import com.scholarspace.admin.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByUser(User user);
    Optional<Instructor> findByUserUserId(Long userId);
}