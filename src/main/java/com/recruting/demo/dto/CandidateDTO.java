package com.recruting.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO {
    
    private Long id;
    
    @NotBlank(message = "Имя кандидата не может быть пустым")
    private String firstName;
    
    @NotBlank(message = "Фамилия кандидата не может быть пустой")
    private String lastName;
    
    @Email(message = "Email должен быть валидным")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    
    @Pattern(regexp = "\\+?[0-9\\s\\-\\(\\)]+", message = "Телефон должен быть валидным")
    @NotBlank(message = "Телефон не может быть пустым")
    private String phone;
    
    private String experience;
    
    private String skills;
    
    private String resumeUrl;
}
