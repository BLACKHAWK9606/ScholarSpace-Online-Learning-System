package com.scholarspace.admin.services;

import com.scholarspace.admin.models.Institution;
import com.scholarspace.admin.repositories.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public InstitutionService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public Institution createInstitution(String name, String location, String contact) {
        // Check if institution with the same name exists
        if (institutionRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Institution with this name already exists");
        }
        
        Institution institution = new Institution();
        institution.setName(name);
        institution.setLocation(location);
        institution.setContact(contact);
        institution.setCreatedAt(LocalDateTime.now());
        
        return institutionRepository.save(institution);
    }

    public Optional<Institution> getInstitutionById(Long id) {
        return institutionRepository.findById(id);
    }

    public Optional<Institution> getInstitutionByName(String name) {
        return institutionRepository.findByName(name);
    }

    public List<Institution> getAllInstitutions() {
        return institutionRepository.findAll();
    }

    public Institution updateInstitution(Institution institution) {
        return institutionRepository.save(institution);
    }

    public void deleteInstitution(Long institutionId) {
        institutionRepository.deleteById(institutionId);
    }
}