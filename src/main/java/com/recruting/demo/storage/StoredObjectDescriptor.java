package com.recruting.demo.storage;

public record StoredObjectDescriptor(
        String objectKey,
        String originalFilename,
        String contentType,
        long size
) {
}
