package com.recruting.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyStatisticsDTO {
    
    private Long vacancyId;
    private String vacancyTitle;
    
    private Long totalApplications;
    private Long submittedCount;
    private Long underReviewCount;
    private Long interviewScheduledCount;
    private Long interviewCompletedCount;
    private Long rejectedCount;
    private Long acceptedCount;
    
    private Long totalOffers;
    private Long pendingOffers;
    private Long acceptedOffers;
    private Long rejectedOffers;
}
