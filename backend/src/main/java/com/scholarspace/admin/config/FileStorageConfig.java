package com.scholarspace.admin.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileStorageConfig {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        // Create base upload directory
        new File(uploadDir).mkdirs();
        
        // Create subdirectories for different types of content
        new File(uploadDir + "course-contents").mkdirs();
        new File(uploadDir + "submissions").mkdirs();
        new File(uploadDir + "profile-pictures").mkdirs();
    }
}