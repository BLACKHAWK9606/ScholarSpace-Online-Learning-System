package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Role;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            return ResponseEntity.ok(userService.getUsersByRole(userRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        
        String email = authentication.getName();
        Optional<User> user = userService.getUserByEmail(email);
        
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user.get());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(HttpServletRequest request, @PathVariable Long id, @RequestParam boolean isActive) {
        if (!"ADMIN".equals(request.getSession().getAttribute("userRole"))) {
            return ResponseEntity.status(403).build();
        }
        
        if (isActive) {
            userService.activateUser(id);
        } else {
            userService.deactivateUser(id);
        }
        
        return ResponseEntity.ok(Map.of("message", isActive ? "User activated successfully" : "User deactivated successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> existingUser = userService.getUserById(id);
        
        if (existingUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = existingUser.get();
        
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userRequest) {
        try {
            String name = (String) userRequest.get("name");
            String email = (String) userRequest.get("email");
            String password = (String) userRequest.get("password");
            String roleStr = (String) userRequest.get("role");
            Boolean isActive = (Boolean) userRequest.get("isActive");
            
            Role role = Role.STUDENT;
            if (roleStr != null && !roleStr.isEmpty()) {
                try {
                    role = Role.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + roleStr));
                }
            }
            
            User user = userService.registerUser(name, email, password, role);
            
            if (isActive != null && !isActive) {
                userService.deactivateUser(user.getUserId());
                user.setActive(false);
            }
            
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}