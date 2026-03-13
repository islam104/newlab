package com.recruting.demo.controller;

import com.recruting.demo.dto.ApplicationDTO;
import com.recruting.demo.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApplicationDTO> createApplication(@Valid @RequestBody ApplicationDTO applicationDTO) {
        ApplicationDTO created = applicationService.createApplication(applicationDTO);
        return ResponseEntity.status(201).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDTO> updateApplication(@PathVariable Long id, 
                                                          @Valid @RequestBody ApplicationDTO applicationDTO) {
        return ResponseEntity.ok(applicationService.updateApplication(id, applicationDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/vacancy/{vacancyId}")
    public ResponseEntity<List<ApplicationDTO>> getApplicationsByVacancy(@PathVariable Long vacancyId) {
        return ResponseEntity.ok(applicationService.getApplicationsByVacancy(vacancyId));
    }
    
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<ApplicationDTO>> getApplicationsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidateId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApplicationDTO>> getApplicationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status));
    }
}
