package com.recruting.demo.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {

    StoredObjectDescriptor uploadSignatureSource(UUID signatureId, MultipartFile file);

    List<PresignedUrlResponse> generateDownloadUrls(List<UUID> signatureIds);
}
