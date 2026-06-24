package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.config.OpenApiProperties;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CommercialOpenApiClient {

    private static final String CATEGORY_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/category.json";
    private static final Map<String, String> KAKAO_CATEGORY_LABELS = new LinkedHashMap<>();

    static {
        KAKAO_CATEGORY_LABELS.put("FD6", "음식");
        KAKAO_CATEGORY_LABELS.put("CE7", "카페");
        KAKAO_CATEGORY_LABELS.put("HP8", "의료");
        KAKAO_CATEGORY_LABELS.put("PM9", "의료");
        KAKAO_CATEGORY_LABELS.put("MT1", "생활");
        KAKAO_CATEGORY_LABELS.put("CS2", "편의");
    }

    private final OpenApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    public CommercialOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
    }

    public List<CommercialPlace> places(double longitude, double latitude, int radiusMeters) {
        List<CommercialPlace> kakaoPlaces = kakaoPlaces(longitude, latitude, radiusMeters);
        return kakaoPlaces.isEmpty() ? SampleData.places(longitude, latitude) : kakaoPlaces;
    }

    private List<CommercialPlace> kakaoPlaces(double longitude, double latitude, int radiusMeters) {
        if (!OpenApiUri.hasText(properties.getKakao().getRestKey())) {
            return List.of();
        }

        List<CommercialPlace> places = new ArrayList<>();
        for (Map.Entry<String, String> category : KAKAO_CATEGORY_LABELS.entrySet()) {
            places.addAll(kakaoCategoryPlaces(category.getKey(), category.getValue(), longitude, latitude, radiusMeters));
        }
        return places.stream()
                .filter(place -> place.longitude() != 0 && place.latitude() != 0)
                .sorted(Comparator.comparing(CommercialPlace::middleCategory).thenComparing(CommercialPlace::name))
                .limit(50)
                .toList();
    }

    private List<CommercialPlace> kakaoCategoryPlaces(
            String categoryCode,
            String largeCategory,
            double longitude,
            double latitude,
            int radiusMeters
    ) {
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(CATEGORY_SEARCH_URL, Map.of(
                            "category_group_code", categoryCode,
                            "x", longitude,
                            "y", latitude,
                            "radius", Math.min(Math.max(radiusMeters, 100), 20000),
                            "sort", "distance",
                            "size", 15
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.getKakao().getRestKey())
                    .retrieve()
                    .body(String.class);
            JsonNode documents = objectMapper.readTree(body).path("documents");
            if (!documents.isArray()) {
                return List.of();
            }
            List<CommercialPlace> places = new ArrayList<>();
            for (JsonNode node : documents) {
                places.add(place(node, largeCategory));
            }
            return places;
        } catch (Exception e) {
            return List.of();
        }
    }

    private CommercialPlace place(JsonNode node, String largeCategory) {
        return new CommercialPlace(
                text(node, "place_name", "상호명 없음"),
                largeCategory,
                middleCategory(node, largeCategory),
                text(node, "road_address_name", text(node, "address_name", "")),
                number(node, "x"),
                number(node, "y")
        );
    }

    private String middleCategory(JsonNode node, String fallback) {
        String categoryName = text(node, "category_name", fallback);
        String[] parts = categoryName.split(">");
        return parts.length == 0 ? fallback : parts[parts.length - 1].trim();
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("");
        return value.isBlank() ? fallback : value;
    }

    private double number(JsonNode node, String field) {
        try {
            return Double.parseDouble(node.path(field).asText("0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
