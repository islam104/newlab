package com.recruting.demo.signature;

import org.springframework.stereotype.Service;
import ru.mfa.signature.JsonCanonicalizer;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SigningService {

    private final SignatureKeyProvider keyProvider;
    private final JsonCanonicalizer canonicalizer;
    private final SignatureProperties properties;

    public SigningService(SignatureKeyProvider keyProvider,
                          JsonCanonicalizer canonicalizer,
                          SignatureProperties properties) {
        this.keyProvider = keyProvider;
        this.canonicalizer = canonicalizer;
        this.properties = properties;
    }

    public String sign(Object payload) {
        byte[] canonicalBytes = canonicalizer.canonizeJson(payload).getBytes(StandardCharsets.UTF_8);
        PrivateKey privateKey = keyProvider.getPrivateKey();

        try {
            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initSign(privateKey);
            signature.update(canonicalBytes);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (GeneralSecurityException ex) {
            throw new SignatureException("Cannot sign payload", ex);
        }
    }
}
