package com.recruting.demo.service;

import com.recruting.demo.dto.OfferDTO;
import com.recruting.demo.entity.Offer;
import com.recruting.demo.repository.OfferRepository;
import com.recruting.demo.repository.ApplicationRepository;
import com.recruting.demo.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferService {
    
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    
    public List<OfferDTO> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public OfferDTO getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Оффер не найден с id: " + id));
        return convertToDTO(offer);
    }
    
    public OfferDTO createOffer(OfferDTO offerDTO) {
        var application = applicationRepository.findById(offerDTO.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        
        var candidate = candidateRepository.findById(offerDTO.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Кандидат не найден"));
        
        Offer offer = new Offer();
        offer.setApplication(application);
        offer.setCandidate(candidate);
        offer.setSalary(offerDTO.getSalary());
        offer.setStartDate(offerDTO.getStartDate());
        offer.setOfferExpirationDate(offerDTO.getOfferExpirationDate());
        offer.setTerms(offerDTO.getTerms());
        offer.setStatus(Offer.OfferStatus.valueOf(offerDTO.getStatus()));
        
        Offer savedOffer = offerRepository.save(offer);
        return convertToDTO(savedOffer);
    }
    
    public OfferDTO updateOffer(Long id, OfferDTO offerDTO) {
        Offer existingOffer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Оффер не найден с id: " + id));
        
        existingOffer.setSalary(offerDTO.getSalary());
        existingOffer.setStartDate(offerDTO.getStartDate());
        existingOffer.setOfferExpirationDate(offerDTO.getOfferExpirationDate());
        existingOffer.setTerms(offerDTO.getTerms());
        existingOffer.setStatus(Offer.OfferStatus.valueOf(offerDTO.getStatus()));
        
        Offer updatedOffer = offerRepository.save(existingOffer);
        return convertToDTO(updatedOffer);
    }
    
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Оффер не найден с id: " + id);
        }
        offerRepository.deleteById(id);
    }
    
    public List<OfferDTO> getOffersByCandidate(Long candidateId) {
        return offerRepository.findByCandidateId(candidateId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<OfferDTO> getOffersByStatus(String status) {
        return offerRepository.findByStatus(Offer.OfferStatus.valueOf(status)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<OfferDTO> getPendingOffers() {
        return offerRepository.findPendingOffers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private OfferDTO convertToDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setApplicationId(offer.getApplication().getId());
        dto.setCandidateId(offer.getCandidate().getId());
        dto.setSalary(offer.getSalary());
        dto.setStartDate(offer.getStartDate());
        dto.setOfferExpirationDate(offer.getOfferExpirationDate());
        dto.setTerms(offer.getTerms());
        dto.setStatus(offer.getStatus().toString());
        dto.setCandidateName(offer.getCandidate().getFirstName() + " " + 
                            offer.getCandidate().getLastName());
        dto.setCandidateEmail(offer.getCandidate().getEmail());
        dto.setVacancyTitle(offer.getApplication().getVacancy().getTitle());
        return dto;
    }
}
