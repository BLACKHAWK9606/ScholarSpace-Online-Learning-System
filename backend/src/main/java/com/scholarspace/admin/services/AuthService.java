package com.scholarspace.admin.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.scholarspace.admin.models.User;
import com.scholarspace.admin.repositories.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;
    
    // Store reset tokens in memory (for development only)
    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       UserService userService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public Map<String, String> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        User user = userOptional.get();
        
        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        // Update last login timestamp
        userService.updateLastLogin(user);
        
        String token = jwtService.generateToken(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getUserId().toString());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().toString());
        
        return response;
    }
    
    /**
     * Generate a password reset token for the given email
     * In a production environment, this would send an email with the reset link
     */
    public Map<String, String> forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        User user = userOptional.get();
        
        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }
        
        // Generate a unique token
        String token = UUID.randomUUID().toString();
        
        // Store token with user email and expiration time (24 hours from now)
        resetTokens.put(token, new PasswordResetToken(email, LocalDateTime.now().plusHours(24)));
        
        // In a real application, send an email with the reset link
        // For development, just return the token
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset token generated successfully");
        response.put("token", token); // Only for development - would normally be sent via email
        response.put("email", email);
        
        return response;
    }
    
    /**
     * Reset the password using the token
     */
    public Map<String, String> resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokens.get(token);
        
        if (resetToken == null) {
            throw new RuntimeException("Invalid or expired password reset token");
        }
        
        if (resetToken.isExpired()) {
            resetTokens.remove(token);
            throw new RuntimeException("Password reset token has expired");
        }
        
        Optional<User> userOptional = userRepository.findByEmail(resetToken.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + resetToken.getEmail());
        }
        
        User user = userOptional.get();
        
        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Remove the used token
        resetTokens.remove(token);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password has been reset successfully");
        
        return response;
    }
    
    /**
     * Validate if a reset token is valid
     */
    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = resetTokens.get(token);
        return resetToken != null && !resetToken.isExpired();
    }
    
    /**
     * Inner class to store password reset token information
     */
    private static class PasswordResetToken {
        private final String email;
        private final LocalDateTime expiryDate;
        
        public PasswordResetToken(String email, LocalDateTime expiryDate) {
            this.email = email;
            this.expiryDate = expiryDate;
        }
        
        public String getEmail() {
            return email;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryDate);
        }
    }
}