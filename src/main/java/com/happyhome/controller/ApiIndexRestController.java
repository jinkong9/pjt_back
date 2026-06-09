package com.happyhome.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "API Index", description = "REST API 진입점")
public class ApiIndexRestController {

    @Operation(summary = "API 링크 목록", description = "주요 REST API와 Swagger 문서 URL을 반환합니다.")
    @GetMapping
    public Map<String, String> index() {
        return Map.of(
                "houses", "/api/houses",
                "recentHouses", "/api/houses/recent",
                "regions", "/api/regions/sidos",
                "rentals", "/api/rentals",
                "analysis", "/api/analysis",
                "notices", "/api/notices",
                "noticePopups", "/api/notices/popups",
                "swagger", "/swagger-ui.html",
                "openapiJson", "/v3/api-docs"
        );
    }
}
