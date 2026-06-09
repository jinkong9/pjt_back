package com.happyhome.openapi;

import java.net.URI;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

public final class OpenApiUri {

    private OpenApiUri() {
    }

    public static URI build(String url, Map<String, ?> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        params.forEach((key, value) -> {
            if (value != null && !value.toString().isBlank()) {
                builder.queryParam(key, value);
            }
        });
        return builder.build().encode().toUri();
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

