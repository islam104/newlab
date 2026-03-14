package com.recruting.demo.license.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class Ticket {
    Instant serverDate;
    long ttlSeconds;
    LocalDateTime activationDate;
    LocalDateTime endingDate;
    Long userId;
    UUID deviceId;
    boolean blocked;
}
