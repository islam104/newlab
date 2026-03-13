package com.recruting.demo.repository;

import com.recruting.demo.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
    List<Interview> findByApplicationId(Long applicationId);
    
    List<Interview> findByInterviewer(String interviewer);
    
    List<Interview> findByStatus(Interview.InterviewStatus status);
    
    List<Interview> findByType(Interview.InterviewType type);
    
    @Query("SELECT i FROM Interview i WHERE i.scheduledTime BETWEEN :startTime AND :endTime")
    List<Interview> findByScheduledTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT i FROM Interview i WHERE i.interviewer = :interviewer AND " +
           "i.scheduledTime BETWEEN :startTime AND :endTime AND i.status != 'CANCELLED'")
    List<Interview> findConflictingInterviews(@Param("interviewer") String interviewer,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT i FROM Interview i WHERE i.scheduledTime > :now ORDER BY i.scheduledTime ASC")
    List<Interview> findUpcomingInterviews(@Param("now") LocalDateTime now);
    
    boolean existsByInterviewerAndScheduledTimeAndStatusNot(String interviewer, 
                                                           LocalDateTime scheduledTime, 
                                                           Interview.InterviewStatus status);
}
