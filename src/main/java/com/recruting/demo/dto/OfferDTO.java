package com.recruting.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferDTO {
    
    private Long id;
    
    @NotNull(message = "ID заявки не может быть null")
    private Long applicationId;
    
    @NotNull(message = "ID кандидата не может быть null")
    private Long candidateId;
    
    @NotNull(message = "Зарплата не может быть null")
    private BigDecimal salary;
    
    private LocalDate startDate;
    
    private LocalDate offerExpirationDate;
    
    private String terms;
    
    private String status;
    
    private String candidateName;
    
    private String candidateEmail;
    
    private String vacancyTitle;
}
