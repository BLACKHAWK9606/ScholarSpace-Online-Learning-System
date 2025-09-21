package com.scholarspace.admin.controllers;

import com.scholarspace.admin.models.Institution;
import com.scholarspace.admin.services.InstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {
    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping
    public ResponseEntity<?> createInstitution(@RequestBody Map<String, String> institutionRequest) {
        try {
            String name = institutionRequest.get("name");
            String location = institutionRequest.get("location");
            String contact = institutionRequest.get("contact");
            
            Institution institution = institutionService.createInstitution(name, location, contact);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInstitutionById(@PathVariable Long id) {
        Optional<Institution> institution = institutionService.getInstitutionById(id);
        return institution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getInstitutionByName(@PathVariable String name) {
        Optional<Institution> institution = institutionService.getInstitutionByName(name);
        return institution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInstitution(@PathVariable Long id, @RequestBody Institution institutionDetails) {
        Optional<Institution> existingInstitution = institutionService.getInstitutionById(id);
        
        if (existingInstitution.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Institution institution = existingInstitution.get();
        
        if (institutionDetails.getName() != null) {
            institution.setName(institutionDetails.getName());
        }
        if (institutionDetails.getLocation() != null) {
            institution.setLocation(institutionDetails.getLocation());
        }
        if (institutionDetails.getContact() != null) {
            institution.setContact(institutionDetails.getContact());
        }
        
        Institution updatedInstitution = institutionService.updateInstitution(institution);
        return ResponseEntity.ok(updatedInstitution);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInstitution(@PathVariable Long id) {
        try {
            institutionService.deleteInstitution(id);
            return ResponseEntity.ok(Map.of("message", "Institution deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}