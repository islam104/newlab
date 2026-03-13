package com.recruting.demo.controller;

import com.recruting.demo.dto.CandidateDTO;
import com.recruting.demo.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {
    
    private final CandidateService candidateService;
    
    @GetMapping
    public ResponseEntity<List<CandidateDTO>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CandidateDTO> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }
    
    @PostMapping
    public ResponseEntity<CandidateDTO> createCandidate(@Valid @RequestBody CandidateDTO candidateDTO) {
        CandidateDTO created = candidateService.createCandidate(candidateDTO);
        return ResponseEntity.status(201).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CandidateDTO> updateCandidate(@PathVariable Long id, 
                                                        @Valid @RequestBody CandidateDTO candidateDTO) {
        return ResponseEntity.ok(candidateService.updateCandidate(id, candidateDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<CandidateDTO> getCandidateByEmail(@PathVariable String email) {
        return ResponseEntity.ok(candidateService.getCandidateByEmail(email));
    }

}
