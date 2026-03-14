package com.recruting.demo.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateLicenseRequest {

    @NotBlank
    private String activationKey;

    @NotBlank
    private String deviceName;

    @NotBlank
    private String deviceMac;
}
