package com.recruting.demo.signature;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Objects;

@Service
public class SignatureKeyProvider {

    private final SignatureProperties properties;
    private volatile KeyMaterial cached;

    public SignatureKeyProvider(SignatureProperties properties) {
        this.properties = properties;
    }

    public PrivateKey getPrivateKey() {
        return load().privateKey();
    }

    public PublicKey getPublicKey() {
        return load().publicKey();
    }

    public X509Certificate getCertificate() {
        return load().certificate();
    }

    private KeyMaterial load() {
        KeyMaterial local = cached;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cached == null) {
                cached = readKeyMaterial();
            }
            return cached;
        }
    }

    private KeyMaterial readKeyMaterial() {
        String keyStorePath = required(properties.getKeyStorePath(), "signature.key-store-path");
        String keyStorePassword = required(properties.getKeyStorePassword(), "signature.key-store-password");
        String keyAlias = required(properties.getKeyAlias(), "signature.key-alias");
        String keyPassword = properties.getKeyPassword();
        String resolvedKeyPassword = (keyPassword == null || keyPassword.isBlank()) ? keyStorePassword : keyPassword;

        try (InputStream input = openKeyStoreStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance(required(properties.getKeyStoreType(), "signature.key-store-type"));
            keyStore.load(input, keyStorePassword.toCharArray());

            Key key = keyStore.getKey(keyAlias, resolvedKeyPassword.toCharArray());
            if (!(key instanceof PrivateKey privateKey)) {
                throw new SignatureException("Alias does not contain a private key: " + keyAlias);
            }

            Certificate certificate = keyStore.getCertificate(keyAlias);
            if (!(certificate instanceof X509Certificate x509Certificate)) {
                throw new SignatureException("Alias does not contain an X.509 certificate: " + keyAlias);
            }

            return new KeyMaterial(privateKey, x509Certificate.getPublicKey(), x509Certificate);
        } catch (IOException ex) {
            throw new SignatureException("Cannot read signature keystore from " + keyStorePath, ex);
        } catch (GeneralSecurityException ex) {
            throw new SignatureException("Cannot load signature key material", ex);
        }
    }

    private InputStream openKeyStoreStream(String location) throws IOException {
        if (location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length());
            return new ClassPathResource(path).getInputStream();
        }
        if (location.startsWith("file:")) {
            return Files.newInputStream(Path.of(URI.create(location)));
        }
        return Files.newInputStream(Path.of(location));
    }

    private String required(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new SignatureException("Missing required signature property: " + propertyName);
        }
        return value;
    }

    private record KeyMaterial(PrivateKey privateKey, PublicKey publicKey, X509Certificate certificate) {

        private KeyMaterial {
            Objects.requireNonNull(privateKey, "privateKey");
            Objects.requireNonNull(publicKey, "publicKey");
            Objects.requireNonNull(certificate, "certificate");
        }
    }
}
