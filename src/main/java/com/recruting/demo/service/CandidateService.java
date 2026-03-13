package com.recruting.demo.service;

import com.recruting.demo.dto.CandidateDTO;
import com.recruting.demo.entity.Candidate;
import com.recruting.demo.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateService {
    
    private final CandidateRepository candidateRepository;
    
    public List<CandidateDTO> getAllCandidates() {
        return candidateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CandidateDTO getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Кандидат не найден с id: " + id));
        return convertToDTO(candidate);
    }
    
    public CandidateDTO createCandidate(CandidateDTO candidateDTO) {
        if (candidateRepository.existsByEmail(candidateDTO.getEmail())) {
            throw new RuntimeException("Кандидат с таким email уже существует");
        }
        
        Candidate candidate = convertToEntity(candidateDTO);
        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToDTO(savedCandidate);
    }
    
    public CandidateDTO updateCandidate(Long id, CandidateDTO candidateDTO) {
        Candidate existingCandidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Кандидат не найден с id: " + id));
        
        if (!existingCandidate.getEmail().equals(candidateDTO.getEmail()) && 
            candidateRepository.existsByEmail(candidateDTO.getEmail())) {
            throw new RuntimeException("Кандидат с таким email уже существует");
        }
        
        existingCandidate.setFirstName(candidateDTO.getFirstName());
        existingCandidate.setLastName(candidateDTO.getLastName());
        existingCandidate.setEmail(candidateDTO.getEmail());
        existingCandidate.setPhone(candidateDTO.getPhone());
        existingCandidate.setExperience(candidateDTO.getExperience());
        existingCandidate.setSkills(candidateDTO.getSkills());
        existingCandidate.setResumeUrl(candidateDTO.getResumeUrl());
        
        Candidate updatedCandidate = candidateRepository.save(existingCandidate);
        return convertToDTO(updatedCandidate);
    }
    
    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new RuntimeException("Кандидат не найден с id: " + id);
        }
        candidateRepository.deleteById(id);
    }
    
    public CandidateDTO getCandidateByEmail(String email) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Кандидат не найден с email: " + email));
        return convertToDTO(candidate);
    }
    
    public List<CandidateDTO> searchCandidates(String keyword) {
        return candidateRepository.searchByKeyword(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CandidateDTO> getCandidatesBySkill(String skill) {
        return candidateRepository.findBySkill(skill).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private CandidateDTO convertToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setEmail(candidate.getEmail());
        dto.setPhone(candidate.getPhone());
        dto.setExperience(candidate.getExperience());
        dto.setSkills(candidate.getSkills());
        dto.setResumeUrl(candidate.getResumeUrl());
        return dto;
    }
    
    private Candidate convertToEntity(CandidateDTO dto) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(dto.getFirstName());
        candidate.setLastName(dto.getLastName());
        candidate.setEmail(dto.getEmail());
        candidate.setPhone(dto.getPhone());
        candidate.setExperience(dto.getExperience());
        candidate.setSkills(dto.getSkills());
        candidate.setResumeUrl(dto.getResumeUrl());
        return candidate;
    }
}
