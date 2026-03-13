package com.recruting.demo.security.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenPairResponse {
    String tokenType;
    String accessToken;
    String refreshToken;
    long accessExpiresInSeconds;
    long refreshExpiresInSeconds;
}
