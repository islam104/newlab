package com.recruting.demo.license.service;

import com.recruting.demo.license.dto.Ticket;
import com.recruting.demo.signature.SignatureVerificationService;
import com.recruting.demo.signature.SigningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketSignerService {

    private final SigningService signingService;
    private final SignatureVerificationService verificationService;

    public String sign(Ticket ticket) {
        String signature = signingService.sign(ticket);
        if (!verificationService.verify(ticket, signature)) {
            throw new IllegalStateException("Cannot verify ticket signature after signing");
        }
        return signature;
    }

    public boolean verify(Ticket ticket, String signature) {
        return verificationService.verify(ticket, signature);
    }
}
