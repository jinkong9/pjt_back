package com.happyhome.transfer.service;

import java.net.URI;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface TransferImageStorage {

    String store(MultipartFile file);

    default String toDisplayUrl(String storedUrl) {
        return storedUrl;
    }

    default String toStoredUrl(String displayUrl) {
        return displayUrl;
    }

    default Optional<URI> redirectUrl(String displayUrl) {
        return Optional.empty();
    }
}
