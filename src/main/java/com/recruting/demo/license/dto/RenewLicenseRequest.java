package com.recruting.demo.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenewLicenseRequest {

    @NotBlank
    private String activationKey;

    @NotBlank
    private String deviceMac;
}
