package com.recruting.demo.license.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class LicenseResponse {
    UUID id;
    String code;
    UUID productId;
    UUID typeId;
    Long ownerId;
    Long userId;
    LocalDateTime firstActivationDate;
    LocalDateTime endingDate;
    boolean blocked;
    int deviceCount;
    String description;
}
