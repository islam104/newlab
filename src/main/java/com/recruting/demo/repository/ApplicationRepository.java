package com.recruting.demo.repository;

import com.recruting.demo.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByVacancyId(Long vacancyId);
    
    List<Application> findByCandidateId(Long candidateId);
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    Optional<Application> findByVacancyIdAndCandidateId(Long vacancyId, Long candidateId);
    
    @Query("SELECT a FROM Application a WHERE a.vacancy.hrManager = :hrManager")
    List<Application> findByHrManager(@Param("hrManager") String hrManager);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.vacancy.id = :vacancyId")
    Long countByVacancyId(@Param("vacancyId") Long vacancyId);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId")
    Long countByCandidateId(@Param("candidateId") Long candidateId);
}
