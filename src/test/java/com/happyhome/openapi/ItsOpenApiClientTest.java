package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ItsOpenApiClientTest {

    @Test
    void buildsEventInfoRequestAroundAnalysisPoint() {
        Map<String, Object> params = ItsOpenApiClient.requestParams("its-key", 126.9413, 37.4826);

        assertThat(params)
                .containsEntry("apiKey", "its-key")
                .containsEntry("type", "all")
                .containsEntry("eventType", "all")
                .containsEntry("getType", "json");
        assertThat((double) params.get("minX")).isLessThan(126.9413);
        assertThat((double) params.get("maxX")).isGreaterThan(126.9413);
        assertThat((double) params.get("minY")).isLessThan(37.4826);
        assertThat((double) params.get("maxY")).isGreaterThan(37.4826);
    }
}

