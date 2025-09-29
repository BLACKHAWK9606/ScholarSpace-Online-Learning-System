package com.scholarspace.admin.config;

import com.scholarspace.admin.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // CRITICAL: Allow all OPTIONS requests (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Dashboard endpoints - Admin gets full stats, others get limited view
                .requestMatchers("/api/dashboard/**").authenticated()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Instructor endpoints  
                .requestMatchers("/api/instructor/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR")
                
                // User Management - FIXED RBAC
                .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}/activate").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}/deactivate").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/users/{id}/status").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/role/{role}").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR")
                
                // Course Management - Refined permissions
                .requestMatchers(HttpMethod.POST, "/api/courses").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR")
                .requestMatchers(HttpMethod.PUT, "/api/courses/{id}").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/{id}").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/courses/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR", "ROLE_STUDENT")
                
                // Enrollment Management
                .requestMatchers("/api/enrollments/student/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STUDENT")
                .requestMatchers("/api/enrollments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR")
                
                // Institution & Department Management
                .requestMatchers("/api/institutions/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/departments/**").hasAuthority("ROLE_ADMIN")
                
                // File Management
                .requestMatchers(HttpMethod.POST, "/api/files/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR")
                .requestMatchers(HttpMethod.GET, "/api/files/**").authenticated()
                
                // Submissions
                .requestMatchers(HttpMethod.POST, "/api/submissions/**").hasAuthority("ROLE_STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/submissions/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_INSTRUCTOR", "ROLE_STUDENT")
                
                // General API endpoints
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}