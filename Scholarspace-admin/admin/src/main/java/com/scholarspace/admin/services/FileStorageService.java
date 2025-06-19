package com.scholarspace.admin.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.FileStorage;
import com.scholarspace.admin.models.User;
import com.scholarspace.admin.repositories.FileStorageRepository;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileStorageRepository fileStorageRepository;

    public FileStorageService(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
        
        // Use a fixed file storage path for better reliability
        this.fileStorageLocation = Paths.get("C:\\ScholarSpace\\uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored: " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Store a file and save its metadata in the database
     */
    public FileStorage storeFile(MultipartFile file, User uploadedBy, FileStorage.FileType fileType, 
                                Course course, boolean isPublic) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File cannot be empty");
            }
            
            // Normalize file name
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..") || originalFileName.contains("/") || originalFileName.contains("\\")) {
                throw new RuntimeException("Filename contains invalid characters: " + originalFileName);
            }
            
            // Generate a unique file name to prevent conflicts
            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
            
            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Create file storage entity
            FileStorage fileStorage = new FileStorage(
                fileName,
                originalFileName,
                file.getContentType(),
                targetLocation.toString(),
                file.getSize(),
                uploadedBy,
                fileType,
                course,
                isPublic
            );
            
            // Save file metadata to database
            return fileStorageRepository.save(fileStorage);
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    /**
     * Load a file as a Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Invalid file path: " + fileName, ex);
        }
    }

    /**
     * Get file metadata by ID
     */
    public Optional<FileStorage> getFileById(Long id) {
        return fileStorageRepository.findById(id);
    }

    /**
     * Get file metadata by filename
     */
    public Optional<FileStorage> getFileByFileName(String fileName) {
        return fileStorageRepository.findByFileName(fileName);
    }

    /**
     * Get all files for a course
     */
    public List<FileStorage> getFilesByCourse(Course course) {
        return fileStorageRepository.findByCourse(course);
    }

    /**
     * Get all files of a specific type for a course
     */
    public List<FileStorage> getFilesByCourseAndType(Course course, FileStorage.FileType fileType) {
        return fileStorageRepository.findByCourseAndFileType(course, fileType);
    }

    /**
     * Get all files uploaded by a user
     */
    public List<FileStorage> getFilesByUser(User user) {
        return fileStorageRepository.findByUploadedBy(user);
    }

    /**
     * Get all public files for a course
     */
    public List<FileStorage> getPublicFilesByCourse(Course course) {
        return fileStorageRepository.findByCourseAndIsPublicTrue(course);
    }

    /**
     * Delete a file by ID
     */
    public void deleteFile(Long id) {
        try {
            Optional<FileStorage> fileOptional = fileStorageRepository.findById(id);
            
            if (fileOptional.isPresent()) {
                FileStorage file = fileOptional.get();
                
                // Delete file from filesystem
                Path filePath = Paths.get(file.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                
                // Delete metadata from database
                fileStorageRepository.delete(file);
            } else {
                throw new RuntimeException("File not found with id: " + id);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error deleting file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Update file metadata
     */
    public FileStorage updateFileMetadata(Long id, FileStorage.FileType fileType, boolean isPublic) {
        try {
            Optional<FileStorage> fileOptional = fileStorageRepository.findById(id);
            
            if (fileOptional.isPresent()) {
                FileStorage file = fileOptional.get();
                file.setFileType(fileType);
                file.setPublic(isPublic);
                
                return fileStorageRepository.save(file);
            } else {
                throw new RuntimeException("File not found with id: " + id);
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException("Error updating file metadata: " + ex.getMessage(), ex);
        }
    }
}
