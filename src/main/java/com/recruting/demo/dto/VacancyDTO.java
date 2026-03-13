package com.recruting.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyDTO {
    
    private Long id;
    
    @NotBlank(message = "Название вакансии не может быть пустым")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Местоположение не может быть пустым")
    private String location;
    
    @NotNull(message = "Зарплата не может быть null")
    @Min(value = 0, message = "Зарплата не может быть отрицательной")
    private BigDecimal salary;
    
    private String status;
    
    @NotBlank(message = "HR менеджер не может быть пустым")
    private String hrManager;
}
