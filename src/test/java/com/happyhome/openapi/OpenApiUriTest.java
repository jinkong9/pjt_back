package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenApiUriTest {

    @Test
    void encodesKoreanQueryParameters() {
        URI uri = OpenApiUri.build("https://example.com/api", Map.of("PAN_SS", "공고중"));

        assertThat(uri.toString()).contains("PAN_SS=%EA%B3%B5%EA%B3%A0%EC%A4%91");
    }
}
