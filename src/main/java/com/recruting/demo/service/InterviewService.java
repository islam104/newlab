package com.recruting.demo.service;

import com.recruting.demo.dto.InterviewDTO;
import com.recruting.demo.entity.Application;
import com.recruting.demo.entity.Interview;
import com.recruting.demo.repository.InterviewRepository;
import com.recruting.demo.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewService {
    
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    
    public List<InterviewDTO> getAllInterviews() {
        return interviewRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public InterviewDTO getInterviewById(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Интервью не найдено с id: " + id));
        return convertToDTO(interview);
    }
    
    public InterviewDTO createInterview(InterviewDTO interviewDTO) {
        Application application = applicationRepository.findById(interviewDTO.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewer(interviewDTO.getInterviewer());
        interview.setScheduledTime(interviewDTO.getScheduledTime());
        interview.setDurationMinutes(interviewDTO.getDurationMinutes());
        interview.setStatus(Interview.InterviewStatus.valueOf(interviewDTO.getStatus()));
        interview.setType(Interview.InterviewType.valueOf(interviewDTO.getType()));
        interview.setNotes(interviewDTO.getNotes());
        
        Interview savedInterview = interviewRepository.save(interview);
        return convertToDTO(savedInterview);
    }
    
    public InterviewDTO updateInterview(Long id, InterviewDTO interviewDTO) {
        Interview existingInterview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Интервью не найдено с id: " + id));
        
        existingInterview.setInterviewer(interviewDTO.getInterviewer());
        existingInterview.setScheduledTime(interviewDTO.getScheduledTime());
        existingInterview.setDurationMinutes(interviewDTO.getDurationMinutes());
        existingInterview.setStatus(Interview.InterviewStatus.valueOf(interviewDTO.getStatus()));
        existingInterview.setType(Interview.InterviewType.valueOf(interviewDTO.getType()));
        existingInterview.setNotes(interviewDTO.getNotes());
        
        Interview updatedInterview = interviewRepository.save(existingInterview);
        return convertToDTO(updatedInterview);
    }
    
    public void deleteInterview(Long id) {
        if (!interviewRepository.existsById(id)) {
            throw new RuntimeException("Интервью не найдено с id: " + id);
        }
        interviewRepository.deleteById(id);
    }
    
    public List<InterviewDTO> getInterviewsByApplication(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<InterviewDTO> getInterviewsByInterviewer(String interviewer) {
        return interviewRepository.findByInterviewer(interviewer).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<InterviewDTO> getUpcomingInterviews() {
        return interviewRepository.findUpcomingInterviews(java.time.LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private InterviewDTO convertToDTO(Interview interview) {
        InterviewDTO dto = new InterviewDTO();
        dto.setId(interview.getId());
        dto.setApplicationId(interview.getApplication().getId());
        dto.setInterviewer(interview.getInterviewer());
        dto.setScheduledTime(interview.getScheduledTime());
        dto.setDurationMinutes(interview.getDurationMinutes());
        dto.setStatus(interview.getStatus().toString());
        dto.setType(interview.getType().toString());
        dto.setNotes(interview.getNotes());
        dto.setCandidateName(interview.getApplication().getCandidate().getFirstName() + " " + 
                            interview.getApplication().getCandidate().getLastName());
        dto.setVacancyTitle(interview.getApplication().getVacancy().getTitle());
        return dto;
    }
}
