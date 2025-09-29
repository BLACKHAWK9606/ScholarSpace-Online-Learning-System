package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.Instructor;
import com.scholarspace.admin.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByUser(User user);
    Optional<Instructor> findByUserUserId(Long userId);
    @Query("SELECT i FROM Instructor i WHERE i.user.isActive = true")
    List<Instructor> findByActiveTrue();
}