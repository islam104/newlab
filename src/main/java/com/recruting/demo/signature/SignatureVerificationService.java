package com.recruting.demo.signature;

import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Service
public class SignatureVerificationService {

    private final SignatureKeyProvider keyProvider;
    private final JsonCanonicalizer canonicalizer;
    private final SignatureProperties properties;
    private volatile PublicKey externalPublicKey;

    public SignatureVerificationService(SignatureKeyProvider keyProvider,
                                        JsonCanonicalizer canonicalizer,
                                        SignatureProperties properties) {
        this.keyProvider = keyProvider;
        this.canonicalizer = canonicalizer;
        this.properties = properties;
    }

    public boolean verify(Object payload, String base64Signature) {
        byte[] canonicalBytes = canonicalizer.canonicalize(payload);
        byte[] signatureBytes;
        try {
            signatureBytes = Base64.getDecoder().decode(base64Signature);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        try {
            Signature verifier = Signature.getInstance(properties.getAlgorithm());
            verifier.initVerify(resolvePublicKey());
            verifier.update(canonicalBytes);
            return verifier.verify(signatureBytes);
        } catch (GeneralSecurityException ex) {
            throw new SignatureException("Cannot verify payload signature", ex);
        }
    }

    private PublicKey resolvePublicKey() {
        String certBase64 = properties.getVerificationPublicCertBase64();
        if (certBase64 == null || certBase64.isBlank()) {
            return keyProvider.getPublicKey();
        }

        PublicKey local = externalPublicKey;
        if (local != null) {
            return local;
        }

        synchronized (this) {
            if (externalPublicKey == null) {
                externalPublicKey = decodePublicKey(certBase64);
            }
            return externalPublicKey;
        }
    }

    private PublicKey decodePublicKey(String certBase64) {
        try {
            byte[] certBytes = Base64.getDecoder().decode(certBase64);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) factory
                    .generateCertificate(new java.io.ByteArrayInputStream(certBytes));
            return certificate.getPublicKey();
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new SignatureException("Invalid signature.verification-public-cert-base64 value", ex);
        }
    }
}
