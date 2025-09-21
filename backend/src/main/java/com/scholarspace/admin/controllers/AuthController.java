package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Role;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.services.AuthService;
import com.scholarspace.admin.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            Map<String, String> response = authService.login(email, password);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String name = registerRequest.get("name");
            String email = registerRequest.get("email");
            String password = registerRequest.get("password");
            String roleStr = registerRequest.get("role");
            
            Role role = Role.STUDENT; // Default role
            if (roleStr != null && !roleStr.isEmpty()) {
                try {
                    role = Role.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + roleStr));
                }
            }
            
            User user = userService.registerUser(name, email, password, role);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getUserId().toString());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> forgotPasswordRequest) {
        try {
            String email = forgotPasswordRequest.get("email");
            
            Map<String, String> response = authService.forgotPassword(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> resetPasswordRequest) {
        try {
            String token = resetPasswordRequest.get("token");
            String newPassword = resetPasswordRequest.get("newPassword");
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reset token is required"));
            }
            
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
            }
            
            Map<String, String> response = authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = authService.validateResetToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}