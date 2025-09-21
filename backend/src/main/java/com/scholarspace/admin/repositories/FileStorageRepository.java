package com.scholarspace.admin.repositories;

import com.scholarspace.admin.models.Course;
import com.scholarspace.admin.models.FileStorage;
import com.scholarspace.admin.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
    
    List<FileStorage> findByCourse(Course course);
    
    List<FileStorage> findByCourseAndFileType(Course course, FileStorage.FileType fileType);
    
    List<FileStorage> findByUploadedBy(User user);
    
    List<FileStorage> findByFileType(FileStorage.FileType fileType);
    
    Optional<FileStorage> findByFileName(String fileName);
    
    List<FileStorage> findByCourseAndIsPublicTrue(Course course);
    
    void deleteByFileName(String fileName);
}
