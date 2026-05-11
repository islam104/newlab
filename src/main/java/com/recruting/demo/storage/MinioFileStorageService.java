package com.recruting.demo.storage;

import com.recruting.demo.malware.entity.MalwareSignature;
import com.recruting.demo.malware.repository.MalwareSignatureRepository;
import com.recruting.demo.security.exception.BadRequestException;
import com.recruting.demo.security.exception.NotFoundException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    private final MalwareSignatureRepository malwareSignatureRepository;

    @Override
    public StoredObjectDescriptor uploadSignatureSource(UUID signatureId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file must not be empty");
        }

        String objectKey = buildObjectKey(signatureId, file.getOriginalFilename());
        String contentType = resolveContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to upload file to object storage", ex);
        }

        return new StoredObjectDescriptor(
                objectKey,
                resolveFilename(file.getOriginalFilename()),
                contentType,
                file.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresignedUrlResponse> generateDownloadUrls(List<UUID> signatureIds) {
        if (signatureIds == null || signatureIds.isEmpty()) {
            throw new BadRequestException("ids must not be empty");
        }

        Map<UUID, MalwareSignature> signaturesById = new LinkedHashMap<>();
        for (MalwareSignature signature : malwareSignatureRepository.findAllById(signatureIds)) {
            signaturesById.put(signature.getId(), signature);
        }
        return signatureIds.stream()
                .distinct()
                .map(id -> {
                    MalwareSignature signature = signaturesById.get(id);
                    if (signature == null) {
                        throw new NotFoundException("Signature not found: " + id);
                    }
                    return signature;
                })
                .map(this::toPresignedUrlResponse)
                .toList();
    }

    private PresignedUrlResponse toPresignedUrlResponse(MalwareSignature signature) {
        if (signature.getStorageObjectKey() == null || signature.getStorageObjectKey().isBlank()) {
            throw new BadRequestException("Source file is not stored for signature: " + signature.getId());
        }

        int expiryMinutes = storageProperties.getPresignedUrlExpiryMinutes();
        Instant expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES);

        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(storageProperties.getBucket())
                            .object(signature.getStorageObjectKey())
                            .expiry(expiryMinutes * 60)
                            .build()
            );
            return new PresignedUrlResponse(signature.getId(), new URL(url), expiresAt);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate pre-signed URL", ex);
        }
    }

    private String buildObjectKey(UUID signatureId, String originalFilename) {
        return "signatures/" + signatureId + "/" + resolveFilename(originalFilename);
    }

    private String resolveFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file.bin";
        }
        return originalFilename.replace("\\", "_").replace("/", "_");
    }

    private String resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_CONTENT_TYPE;
        }
        return contentType;
    }
}
