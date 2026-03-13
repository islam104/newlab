package com.recruting.demo.service;

import com.recruting.demo.dto.ApplicationDTO;
import com.recruting.demo.entity.Application;
import com.recruting.demo.entity.Candidate;
import com.recruting.demo.entity.Vacancy;
import com.recruting.demo.repository.ApplicationRepository;
import com.recruting.demo.repository.VacancyRepository;
import com.recruting.demo.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final CandidateRepository candidateRepository;
    
    public List<ApplicationDTO> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ApplicationDTO getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена с id: " + id));
        return convertToDTO(application);
    }
    
    public ApplicationDTO createApplication(ApplicationDTO applicationDTO) {
        Vacancy vacancy = vacancyRepository.findById(applicationDTO.getVacancyId())
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));
        
        Candidate candidate = candidateRepository.findById(applicationDTO.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Кандидат не найден"));
        
        if (applicationRepository.findByVacancyIdAndCandidateId(
                applicationDTO.getVacancyId(), applicationDTO.getCandidateId()).isPresent()) {
            throw new RuntimeException("Заявка уже существует");
        }
        
        Application application = new Application();
        application.setVacancy(vacancy);
        application.setCandidate(candidate);
        application.setCoverLetter(applicationDTO.getCoverLetter());
        application.setStatus(Application.ApplicationStatus.SUBMITTED);
        
        Application savedApplication = applicationRepository.save(application);
        return convertToDTO(savedApplication);
    }
    
    public ApplicationDTO updateApplication(Long id, ApplicationDTO applicationDTO) {
        Application existingApplication = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена с id: " + id));
        
        existingApplication.setCoverLetter(applicationDTO.getCoverLetter());
        if (applicationDTO.getStatus() != null) {
            existingApplication.setStatus(Application.ApplicationStatus.valueOf(applicationDTO.getStatus()));
        }
        
        Application updatedApplication = applicationRepository.save(existingApplication);
        return convertToDTO(updatedApplication);
    }
    
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("Заявка не найдена с id: " + id);
        }
        applicationRepository.deleteById(id);
    }
    
    public List<ApplicationDTO> getApplicationsByVacancy(Long vacancyId) {
        return applicationRepository.findByVacancyId(vacancyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ApplicationDTO> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ApplicationDTO> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(Application.ApplicationStatus.valueOf(status)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private ApplicationDTO convertToDTO(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setVacancyId(application.getVacancy().getId());
        dto.setCandidateId(application.getCandidate().getId());
        dto.setCoverLetter(application.getCoverLetter());
        dto.setStatus(application.getStatus().toString());
        dto.setVacancyTitle(application.getVacancy().getTitle());
        dto.setCandidateName(application.getCandidate().getFirstName() + " " + 
                            application.getCandidate().getLastName());
        dto.setCandidateEmail(application.getCandidate().getEmail());
        return dto;
    }
}
