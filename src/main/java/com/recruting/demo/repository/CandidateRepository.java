package com.recruting.demo.repository;

import com.recruting.demo.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByEmail(String email);
    
    List<Candidate> findByFirstNameContainingIgnoreCase(String firstName);
    
    List<Candidate> findByLastNameContainingIgnoreCase(String lastName);
    
    @Query("SELECT c FROM Candidate c WHERE c.skills LIKE %:skill%")
    List<Candidate> findBySkill(@Param("skill") String skill);
    
    @Query("SELECT c FROM Candidate c WHERE c.firstName LIKE %:keyword% OR c.lastName LIKE %:keyword% OR c.skills LIKE %:keyword%")
    List<Candidate> searchByKeyword(@Param("keyword") String keyword);
    
    boolean existsByEmail(String email);
}
