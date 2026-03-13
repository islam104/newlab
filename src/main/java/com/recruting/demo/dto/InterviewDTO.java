package com.recruting.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDTO {
    
    private Long id;
    
    @NotNull(message = "ID заявки не может быть null")
    private Long applicationId;
    
    @NotBlank(message = "Имя интервьюера не может быть пустым")
    private String interviewer;
    
    @NotNull(message = "Время интервью не может быть null")
    private LocalDateTime scheduledTime;
    
    private Integer durationMinutes;
    
    private String status;
    
    private String type;
    
    private String notes;
    
    private String candidateName;
    
    private String vacancyTitle;
}
