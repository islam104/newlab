package com.recruting.demo.license.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckLicenseRequest {

    @NotNull
    private UUID productId;

    @NotBlank
    private String deviceMac;
}
