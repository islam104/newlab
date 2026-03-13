package com.recruting.demo.controller;

import com.recruting.demo.dto.OfferDTO;
import com.recruting.demo.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {
    
    private final OfferService offerService;
    
    @GetMapping
    public ResponseEntity<List<OfferDTO>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }
    
    @PostMapping
    public ResponseEntity<OfferDTO> createOffer(@Valid @RequestBody OfferDTO offerDTO) {
        OfferDTO created = offerService.createOffer(offerDTO);
        return ResponseEntity.status(201).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OfferDTO> updateOffer(@PathVariable Long id, 
                                                 @Valid @RequestBody OfferDTO offerDTO) {
        return ResponseEntity.ok(offerService.updateOffer(id, offerDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<OfferDTO>> getOffersByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(offerService.getOffersByCandidate(candidateId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OfferDTO>> getOffersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(offerService.getOffersByStatus(status));
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<OfferDTO>> getPendingOffers() {
        return ResponseEntity.ok(offerService.getPendingOffers());
    }
}
