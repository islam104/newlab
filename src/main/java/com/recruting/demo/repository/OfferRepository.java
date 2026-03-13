package com.recruting.demo.repository;

import com.recruting.demo.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    
    List<Offer> findByCandidateId(Long candidateId);
    
    List<Offer> findByApplicationId(Long applicationId);
    
    List<Offer> findByStatus(Offer.OfferStatus status);
    
    Optional<Offer> findByCandidateIdAndStatus(Long candidateId, Offer.OfferStatus status);
    
    @Query("SELECT o FROM Offer o WHERE o.offerExpirationDate < :currentDate AND o.status = 'PENDING'")
    List<Offer> findExpiredOffers(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(o) FROM Offer o WHERE o.candidate.id = :candidateId AND o.status = 'ACCEPTED'")
    Long countAcceptedOffersByCandidate(@Param("candidateId") Long candidateId);
    
    @Query("SELECT o FROM Offer o WHERE o.status = 'PENDING' ORDER BY o.createdAt DESC")
    List<Offer> findPendingOffers();
    
    boolean existsByCandidateIdAndStatus(Long candidateId, Offer.OfferStatus status);
}
