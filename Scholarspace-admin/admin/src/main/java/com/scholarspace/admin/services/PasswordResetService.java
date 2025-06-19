package com.scholarspace.admin.services;

import com.scholarspace.admin.models.PasswordResetToken;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.repositories.PasswordResetTokenRepository;
import com.scholarspace.admin.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // Default token expiration time in minutes
    private final int tokenExpirationMinutes = 30;
    
    // Frontend URL for reset link
    private final String frontendUrl = "http://localhost:3000";

    public PasswordResetService(UserRepository userRepository, 
                             PasswordResetTokenRepository tokenRepository,
                             PasswordEncoder passwordEncoder,
                             EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Creates a password reset token for the user with the given email
     * and sends a reset link to their email.
     *
     * @param email The email of the user requesting a password reset
     * @return true if the reset token was created and email sent, false otherwise
     */
    @Transactional
    public boolean createPasswordResetTokenAndSendEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Don't reveal that the email doesn't exist for security reasons
            return false;
        }
        
        User user = userOptional.get();
        
        // Invalidate any existing tokens for this user
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUserAndUsedFalse(user);
        existingToken.ifPresent(token -> {
            token.setUsed(true);
            tokenRepository.save(token);
        });
        
        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setCreatedAt(LocalDateTime.now());
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        
        tokenRepository.save(passwordResetToken);
        
        // Send email with reset link
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "ScholarSpace Password Reset";
        String body = "Hello " + user.getName() + ",\n\n" +
                "You have requested to reset your password. Please click the link below to set a new password:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in " + tokenExpirationMinutes + " minutes.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Regards,\nScholarSpace Team";
        
        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            return true;
        } catch (Exception e) {
            // Log the error but don't expose it to the client
            System.err.println("Failed to send password reset email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates a password reset token and resets the user's password if valid.
     *
     * @param token The password reset token
     * @param newPassword The new password to set
     * @return true if the password was reset successfully, false otherwise
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (!resetToken.isValid()) {
            return false;
        }
        
        // Update the user's password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return true;
    }
}
