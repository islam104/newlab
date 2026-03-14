package com.recruting.demo.license.service;

import com.recruting.demo.license.dto.Ticket;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TicketSignerService {

    @Value("${security.ticket.secret}")
    private String ticketSecret;

    public String sign(Ticket ticket) {
        String payload = buildPayload(ticket);
        try {
            byte[] keyBytes = Decoders.BASE64.decode(ticketSecret);
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot sign ticket", ex);
        }
    }

    private String buildPayload(Ticket ticket) {
        Instant serverDate = ticket.getServerDate();
        String activation = ticket.getActivationDate() == null ? "" : ticket.getActivationDate().toString();
        String ending = ticket.getEndingDate() == null ? "" : ticket.getEndingDate().toString();
        String deviceId = ticket.getDeviceId() == null ? "" : ticket.getDeviceId().toString();
        String userId = ticket.getUserId() == null ? "" : String.valueOf(ticket.getUserId());
        return String.join("|",
                serverDate == null ? "" : String.valueOf(serverDate.getEpochSecond()),
                String.valueOf(ticket.getTtlSeconds()),
                activation,
                ending,
                userId,
                deviceId,
                String.valueOf(ticket.isBlocked())
        );
    }
}
