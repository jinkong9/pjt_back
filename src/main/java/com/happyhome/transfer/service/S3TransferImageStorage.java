package com.happyhome.transfer.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3TransferImageStorage implements TransferImageStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String region;
    private final String publicBaseUrl;
    private final String defaultBaseUrl;

    public S3TransferImageStorage(S3Client s3Client, String bucket, String region, String publicBaseUrl) {
        this(s3Client, null, bucket, region, publicBaseUrl);
    }

    public S3TransferImageStorage(
            S3Client s3Client,
            S3Presigner s3Presigner,
            String bucket,
            String region,
            String publicBaseUrl
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.region = region;
        this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
        this.defaultBaseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    @Override
    public String store(MultipartFile file) {
        String key = "transfers/" + UUID.randomUUID() + extension(file);
        try {
            PutObjectRequest.Builder request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentLength(file.getSize());
            if (StringUtils.hasText(file.getContentType())) {
                request.contentType(file.getContentType());
            }
            s3Client.putObject(request.build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return publicUrl(key);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public String toDisplayUrl(String storedUrl) {
        if (!StringUtils.hasText(storedUrl) || keyFromStoredUrl(storedUrl).isEmpty()) {
            return storedUrl;
        }
        return "/api/transfers/images?url=" + URLEncoder.encode(storedUrl, StandardCharsets.UTF_8);
    }

    @Override
    public String toStoredUrl(String displayUrl) {
        if (!StringUtils.hasText(displayUrl)) {
            return displayUrl;
        }
        int queryIndex = displayUrl.indexOf("?url=");
        if (queryIndex < 0 || !displayUrl.contains("/api/transfers/images")) {
            return displayUrl;
        }
        String encodedUrl = displayUrl.substring(queryIndex + 5);
        int nextParameterIndex = encodedUrl.indexOf('&');
        if (nextParameterIndex >= 0) {
            encodedUrl = encodedUrl.substring(0, nextParameterIndex);
        }
        return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);
    }

    @Override
    public Optional<URI> redirectUrl(String displayUrl) {
        if (s3Presigner == null) {
            return Optional.empty();
        }
        return keyFromStoredUrl(toStoredUrl(displayUrl))
                .map(key -> {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();
                    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getObjectRequest)
                            .build();
                    return URI.create(s3Presigner.presignGetObject(presignRequest).url().toString());
                });
    }

    private String publicUrl(String key) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl + "/" + key;
        }
        return defaultBaseUrl + "/" + key;
    }

    private Optional<String> keyFromStoredUrl(String storedUrl) {
        String normalizedUrl = toStoredCandidate(storedUrl);
        if (normalizedUrl.startsWith("transfers/")) {
            return Optional.of(normalizedUrl);
        }
        Optional<String> key = keyAfterBaseUrl(normalizedUrl, defaultBaseUrl);
        if (key.isEmpty() && StringUtils.hasText(publicBaseUrl)) {
            key = keyAfterBaseUrl(normalizedUrl, publicBaseUrl);
        }
        return key.filter(value -> value.startsWith("transfers/"));
    }

    private Optional<String> keyAfterBaseUrl(String url, String baseUrl) {
        if (!StringUtils.hasText(baseUrl) || !url.startsWith(baseUrl + "/")) {
            return Optional.empty();
        }
        return Optional.of(url.substring(baseUrl.length() + 1));
    }

    private String toStoredCandidate(String url) {
        return StringUtils.hasText(url) ? url.trim() : "";
    }

    private String normalizeBaseUrl(String baseUrl) {
        return StringUtils.hasText(baseUrl) ? baseUrl.replaceAll("/+$", "") : "";
    }

    private String extension(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        int dotIndex = originalName.lastIndexOf('.');
        return dotIndex >= 0 ? originalName.substring(dotIndex) : "";
    }
}
