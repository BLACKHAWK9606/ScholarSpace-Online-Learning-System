package com.scholarspace.admin.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.scholarspace.admin.models.User;
import com.scholarspace.admin.repositories.UserRepository;
import com.scholarspace.admin.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // Get the User entity from repository
                Optional<User> userOptional = userRepository.findByEmail(userEmail);
                
                if (userOptional.isPresent() && jwtService.validateToken(jwt, userOptional.get())) {
                    User user = userOptional.get();
                    
                    // FIXED: Extract role from JWT and ensure ROLE_ prefix
                    String role = jwtService.extractClaim(jwt, claims -> 
                        claims.get("role", String.class));
                    
                    // Ensure role has ROLE_ prefix for Spring Security
                    String springRole = role;
                    if (role != null && !role.startsWith("ROLE_")) {
                        springRole = "ROLE_" + role;
                    }
                    
                    // FALLBACK: If JWT doesn't have role, use role from User entity
                    if (springRole == null) {
                        springRole = "ROLE_" + user.getRole().name();
                    }
                    
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(springRole);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        Collections.singletonList(authority)
                    );
                    
                    // Add user info to authentication details for easy access
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Debug logging (remove in production)
                    logger.debug("Authentication successful for user: " + userEmail + " with role: " + springRole);
                }
            }
        } catch (Exception e) {
            logger.error("JWT Authentication Error for token: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...", e);
            // Clear security context on any error
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}