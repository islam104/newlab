package com.recruting.demo.license.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateLicenseRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID typeId;

    @NotNull
    private Long ownerId;

    @Positive
    private int deviceCount = 1;

    private String description;
}
