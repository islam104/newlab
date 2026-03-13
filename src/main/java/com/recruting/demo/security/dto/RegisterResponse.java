package com.recruting.demo.security.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
}
