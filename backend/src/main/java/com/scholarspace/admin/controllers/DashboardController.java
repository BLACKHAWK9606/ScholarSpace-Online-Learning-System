package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.Role;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.repositories.CourseRepository;
import com.scholarspace.admin.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public DashboardController(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get user statistics
        List<User> allUsers = userRepository.findAll();
        stats.put("totalUsers", allUsers.size());
        
        // Count users by role
        long instructorCount = allUsers.stream()
                .filter(user -> Role.INSTRUCTOR == user.getRole())
                .count();
        stats.put("totalInstructors", instructorCount);
        
        long studentCount = allUsers.stream()
                .filter(user -> Role.STUDENT == user.getRole())
                .count();
        stats.put("totalStudents", studentCount);
        
        // Count active users
        long activeUsers = allUsers.stream()
                .filter(User::isActive)
                .count();
        stats.put("activeUsers", activeUsers);
        
        // Get recently registered users (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<User> recentUsers = allUsers.stream()
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());
        stats.put("recentUsers", recentUsers.size());
        
        // Get course statistics
        List<Course> allCourses = courseRepository.findAll();
        stats.put("totalCourses", allCourses.size());
        
        // Count active courses
        long activeCourses = allCourses.stream()
                .filter(Course::isActive)
                .count();
        stats.put("activeCourses", activeCourses);
        
        // Get recently added courses (last 30 days)
        List<Course> recentCourses = allCourses.stream()
                .filter(course -> course.getCreatedAt() != null && course.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());
        stats.put("recentCourses", recentCourses.size());
        
        return ResponseEntity.ok(stats);
    }
}
