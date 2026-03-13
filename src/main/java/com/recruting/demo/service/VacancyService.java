package com.recruting.demo.service;

import com.recruting.demo.dto.VacancyDTO;
import com.recruting.demo.entity.Vacancy;
import com.recruting.demo.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VacancyService {
    
    private final VacancyRepository vacancyRepository;
    
    public List<VacancyDTO> getAllVacancies() {
        return vacancyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public VacancyDTO getVacancyById(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена с id: " + id));
        return convertToDTO(vacancy);
    }
    
    public VacancyDTO createVacancy(VacancyDTO vacancyDTO) {
        Vacancy vacancy = convertToEntity(vacancyDTO);
        Vacancy savedVacancy = vacancyRepository.save(vacancy);
        return convertToDTO(savedVacancy);
    }
    
    public VacancyDTO updateVacancy(Long id, VacancyDTO vacancyDTO) {
        Vacancy existingVacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена с id: " + id));
        
        existingVacancy.setTitle(vacancyDTO.getTitle());
        existingVacancy.setDescription(vacancyDTO.getDescription());
        existingVacancy.setLocation(vacancyDTO.getLocation());
        existingVacancy.setSalary(vacancyDTO.getSalary());
        existingVacancy.setStatus(Vacancy.VacancyStatus.valueOf(vacancyDTO.getStatus()));
        existingVacancy.setHrManager(vacancyDTO.getHrManager());
        
        Vacancy updatedVacancy = vacancyRepository.save(existingVacancy);
        return convertToDTO(updatedVacancy);
    }
    
    public void deleteVacancy(Long id) {
        if (!vacancyRepository.existsById(id)) {
            throw new RuntimeException("Вакансия не найдена с id: " + id);
        }
        vacancyRepository.deleteById(id);
    }
    
    public List<VacancyDTO> getActiveVacancies() {
        return vacancyRepository.findByStatus(Vacancy.VacancyStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<VacancyDTO> getVacanciesByHrManager(String hrManager) {
        return vacancyRepository.findByHrManager(hrManager).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<VacancyDTO> searchVacancies(String keyword) {
        return vacancyRepository.searchByKeyword(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private VacancyDTO convertToDTO(Vacancy vacancy) {
        VacancyDTO dto = new VacancyDTO();
        dto.setId(vacancy.getId());
        dto.setTitle(vacancy.getTitle());
        dto.setDescription(vacancy.getDescription());
        dto.setLocation(vacancy.getLocation());
        dto.setSalary(vacancy.getSalary());
        dto.setStatus(vacancy.getStatus().toString());
        dto.setHrManager(vacancy.getHrManager());
        return dto;
    }
    
    private Vacancy convertToEntity(VacancyDTO dto) {
        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(dto.getTitle());
        vacancy.setDescription(dto.getDescription());
        vacancy.setLocation(dto.getLocation());
        vacancy.setSalary(dto.getSalary());
        vacancy.setStatus(dto.getStatus() != null ? 
                Vacancy.VacancyStatus.valueOf(dto.getStatus()) : Vacancy.VacancyStatus.ACTIVE);
        vacancy.setHrManager(dto.getHrManager());
        return vacancy;
    }
}
