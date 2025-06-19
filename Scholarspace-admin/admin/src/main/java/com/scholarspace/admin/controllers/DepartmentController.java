package com.scholarspace.admin.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scholarspace.admin.models.Department;
import com.scholarspace.admin.services.DepartmentService;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, Object> departmentRequest) {
        try {
            String name = (String) departmentRequest.get("name");
            String code = (String) departmentRequest.get("code");
            String description = (String) departmentRequest.get("description");
            Long institutionId = Long.valueOf(departmentRequest.get("institutionId").toString());
            
            Department department = departmentService.createDepartment(name, code, description, institutionId);
            return ResponseEntity.ok(department);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        return department.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getDepartmentByCode(@PathVariable String code) {
        Optional<Department> department = departmentService.getDepartmentByCode(code);
        return department.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<Department>> getDepartmentsByInstitution(@PathVariable Long institutionId) {
        return ResponseEntity.ok(departmentService.getDepartmentsByInstitution(institutionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        Optional<Department> existingDepartment = departmentService.getDepartmentById(id);
        
        if (existingDepartment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Department department = existingDepartment.get();
        
        if (departmentDetails.getName() != null) {
            department.setName(departmentDetails.getName());
        }
        if (departmentDetails.getDescription() != null) {
            department.setDescription(departmentDetails.getDescription());
        }
        
        Department updatedDepartment = departmentService.updateDepartment(department);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}