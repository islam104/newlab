package com.recruting.demo.controller;

import com.recruting.demo.dto.VacancyDTO;
import com.recruting.demo.service.VacancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {
    
    private final VacancyService vacancyService;
    
    @GetMapping
    public ResponseEntity<List<VacancyDTO>> getAllVacancies() {
        return ResponseEntity.ok(vacancyService.getAllVacancies());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VacancyDTO> getVacancyById(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.getVacancyById(id));
    }
    
    @PostMapping
    public ResponseEntity<VacancyDTO> createVacancy(@Valid @RequestBody VacancyDTO vacancyDTO) {
        VacancyDTO created = vacancyService.createVacancy(vacancyDTO);
        return ResponseEntity.status(201).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<VacancyDTO> updateVacancy(@PathVariable Long id, 
                                                   @Valid @RequestBody VacancyDTO vacancyDTO) {
        return ResponseEntity.ok(vacancyService.updateVacancy(id, vacancyDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("get/active")
    public ResponseEntity<List<VacancyDTO>> getActiveVacancies() {
        return ResponseEntity.ok(vacancyService.getActiveVacancies());
    }
    
    @GetMapping("/hr-manager/{hrManager}")
    public ResponseEntity<List<VacancyDTO>> getVacanciesByHrManager(@PathVariable String hrManager) {
        return ResponseEntity.ok(vacancyService.getVacanciesByHrManager(hrManager));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<VacancyDTO>> searchVacancies(@RequestParam String keyword) {
        return ResponseEntity.ok(vacancyService.searchVacancies(keyword));
    }
}
