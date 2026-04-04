package com.recruting.demo.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.recruting.demo.license.dto.Ticket;
import com.recruting.demo.license.service.TicketSignerService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureModuleTest {

    @Test
    void canonicalizerBuildsDeterministicJson() {
        JsonCanonicalizer canonicalizer = new JsonCanonicalizer(configuredMapper());

        byte[] first = canonicalizer.canonicalize(Map.of("b", 2, "a", "x", "n", 1.2300));
        byte[] second = canonicalizer.canonicalize(Map.of("n", 1.23, "a", "x", "b", 2));

        assertEquals(new String(first, StandardCharsets.UTF_8), new String(second, StandardCharsets.UTF_8));
        assertEquals("{\"a\":\"x\",\"b\":2,\"n\":1.23}", new String(first, StandardCharsets.UTF_8));
    }

    @Test
    void ticketSignatureIsCreatedAndVerifiedWithPublicCertificate() throws Exception {
        SignatureProperties properties = new SignatureProperties();
        properties.setKeyStorePath("classpath:signature/test-signing.p12");
        properties.setKeyStoreType("PKCS12");
        properties.setKeyStorePassword("changeit");
        properties.setKeyAlias("test-signing");
        properties.setKeyPassword("changeit");
        properties.setAlgorithm("SHA256withRSA");

        JsonCanonicalizer canonicalizer = new JsonCanonicalizer(configuredMapper());
        SignatureKeyProvider keyProvider = new SignatureKeyProvider(properties);
        SigningService signingService = new SigningService(keyProvider, canonicalizer, properties);
        SignatureVerificationService verificationService = new SignatureVerificationService(keyProvider, canonicalizer, properties);
        TicketSignerService signer = new TicketSignerService(signingService, verificationService);

        Ticket ticket = Ticket.builder()
                .serverDate(Instant.parse("2026-03-27T12:00:00Z"))
                .ttlSeconds(300)
                .activationDate(LocalDateTime.parse("2026-03-01T10:15:30"))
                .endingDate(LocalDateTime.parse("2026-04-01T10:15:30"))
                .userId(42L)
                .deviceId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .blocked(false)
                .build();

        String signature = signer.sign(ticket);
        assertTrue(signer.verify(ticket, signature));

        String certBase64 = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/signature/test-signing.crt"))
        );
        properties.setVerificationPublicCertBase64(certBase64);

        SignatureVerificationService verificationByExternalCert =
                new SignatureVerificationService(keyProvider, canonicalizer, properties);
        assertTrue(verificationByExternalCert.verify(ticket, signature));

        Ticket tampered = Ticket.builder()
                .serverDate(ticket.getServerDate())
                .ttlSeconds(299)
                .activationDate(ticket.getActivationDate())
                .endingDate(ticket.getEndingDate())
                .userId(ticket.getUserId())
                .deviceId(ticket.getDeviceId())
                .blocked(ticket.isBlocked())
                .build();

        assertFalse(verificationByExternalCert.verify(tampered, signature));
    }

    private ObjectMapper configuredMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
