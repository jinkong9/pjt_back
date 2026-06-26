package com.happyhome.rental.email.controller;

import com.happyhome.rental.email.service.RentalNoticeEmailService;
import com.happyhome.rental.recommendation.service.RentalRecommendationService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
public class RentalNoticeEmailRestController {

    private final RentalNoticeEmailService rentalNoticeEmailService;

    public RentalNoticeEmailRestController(RentalNoticeEmailService rentalNoticeEmailService) {
        this.rentalNoticeEmailService = rentalNoticeEmailService;
    }

    @PostMapping("/favorites/emails/send")
    public ResponseEntity<RentalNoticeEmailService.EmailRunResult> send(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RentalNoticeEmailService.EmailRunResult result =
                rentalNoticeEmailService.sendFavoriteNoticeEmails(authentication.getName());
        if (result.consentRequiredCount() > 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recommendations/emails/send")
    public ResponseEntity<?> sendRecommendations(
            @RequestBody(required = false) RecommendationEmailRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RecommendationEmailRequest emailRequest = request == null ? RecommendationEmailRequest.empty() : request;
        try {
            RentalNoticeEmailService.EmailRunResult result =
                    rentalNoticeEmailService.sendRecommendedNoticeEmails(
                            authentication.getName(),
                            emailRequest.toCriteria(),
                            emailRequest.normalizedLimit()
                    );
            if (result.consentRequiredCount() > 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    public record RecommendationEmailRequest(
            List<String> desiredRegions,
            List<String> rentalTypes,
            Integer limit
    ) {
        static RecommendationEmailRequest empty() {
            return new RecommendationEmailRequest(List.of(), List.of(), 5);
        }

        RentalRecommendationService.RecommendationCriteria toCriteria() {
            return new RentalRecommendationService.RecommendationCriteria(desiredRegions, rentalTypes);
        }

        int normalizedLimit() {
            return limit == null ? 5 : Math.min(Math.max(limit, 1), 10);
        }
    }
}
