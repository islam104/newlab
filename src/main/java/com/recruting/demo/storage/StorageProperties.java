package com.recruting.demo.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String endpoint = "http://localhost:9000";
    private String accessKey = "newsem-service";
    private String secretKey = "newsem-service-secret";
    private String bucket = "malware-signatures";
    private int presignedUrlExpiryMinutes = 60;
    private int firstBytesLength = 16;
}
