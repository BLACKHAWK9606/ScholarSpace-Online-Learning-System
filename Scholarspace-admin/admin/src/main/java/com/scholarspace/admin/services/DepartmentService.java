package com.scholarspace.admin.services;

import com.scholarspace.admin.models.Department;
import com.scholarspace.admin.models.Institution;
import com.scholarspace.admin.repositories.DepartmentRepository;
import com.scholarspace.admin.repositories.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                            InstitutionRepository institutionRepository) {
        this.departmentRepository = departmentRepository;
        this.institutionRepository = institutionRepository;
    }

    public Department createDepartment(String name, String code, String description, Long institutionId) {
        // Check if department code already exists
        if (departmentRepository.findByCode(code).isPresent()) {
            throw new RuntimeException("Department code already exists");
        }
        
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        
        Department department = new Department();
        department.setName(name);
        department.setCode(code);
        department.setDescription(description);
        department.setInstitution(institution);
        department.setCreatedAt(LocalDateTime.now());
        
        return departmentRepository.save(department);
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Optional<Department> getDepartmentByCode(String code) {
        return departmentRepository.findByCode(code);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Department> getDepartmentsByInstitution(Long institutionId) {
        return departmentRepository.findByInstitution_InstitutionId(institutionId);
    }

    public Department updateDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public void deleteDepartment(Long departmentId) {
        departmentRepository.deleteById(departmentId);
    }
}