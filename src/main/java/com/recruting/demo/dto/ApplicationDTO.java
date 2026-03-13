package com.recruting.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    
    private Long id;
    
    @NotNull(message = "ID вакансии не может быть null")
    private Long vacancyId;
    
    @NotNull(message = "ID кандидата не может быть null")
    private Long candidateId;
    
    private String coverLetter;
    
    private String status;
    
    private String vacancyTitle;
    
    private String candidateName;
    
    private String candidateEmail;
}
