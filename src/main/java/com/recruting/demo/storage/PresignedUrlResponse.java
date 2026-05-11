package com.recruting.demo.storage;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

public record PresignedUrlResponse(
        UUID signatureId,
        URL url,
        Instant expiresAt
) {
}
