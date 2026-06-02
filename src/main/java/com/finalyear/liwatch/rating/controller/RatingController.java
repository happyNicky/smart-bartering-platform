package com.finalyear.liwatch.rating.controller;

import com.finalyear.liwatch.rating.dto.RatingResponseDto;
import com.finalyear.liwatch.rating.dto.RatingWindowDto;
import com.finalyear.liwatch.rating.dto.SubmitRatingRequest;
import com.finalyear.liwatch.rating.dto.UpdateRatingRequest;
import com.finalyear.liwatch.rating.service.RatingPublicationService;
import com.finalyear.liwatch.rating.service.RatingService;
import com.finalyear.liwatch.trust.dto.BadgeHistoryDto;
import com.finalyear.liwatch.trust.dto.TrustResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class RatingController {

    private final RatingService ratingService;
    private final RatingPublicationService ratingPublicationService;

    @Value("${liwatch.internal.api-key:}")
    private String internalApiKey;

    public RatingController(RatingService ratingService, RatingPublicationService ratingPublicationService) {
        this.ratingService = ratingService;
        this.ratingPublicationService = ratingPublicationService;
    }

    @GetMapping("/api/ratings/barter/{barterId}/window")
    public ResponseEntity<RatingWindowDto> getRatingWindowDetails(@PathVariable Long barterId) {
        return ResponseEntity.ok(ratingService.getRatingWindowDetails(barterId));
    }

    @GetMapping("/api/ratings/cycle-barter/{cycleBarterId}")
    public ResponseEntity<List<RatingResponseDto>> getCycleBarterRatings(@PathVariable Long cycleBarterId) {
        return ResponseEntity.ok(ratingService.getCycleBarterRatings(cycleBarterId));
    }

    @PostMapping("/api/ratings")
    public ResponseEntity<?> submitRating(@Valid @RequestBody SubmitRatingRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.submitRating(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Rating failed: " + e.getMessage() + " | Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "None")));
        }
    }

    @PutMapping("/api/ratings/{ratingId}")
    public ResponseEntity<RatingResponseDto> updateRating(
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        return ResponseEntity.ok(ratingService.updateRating(ratingId, request));
    }

    @GetMapping("/api/users/{userId}/ratings")
    public ResponseEntity<List<RatingResponseDto>> getUserRatings(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getPublishedRatingsForUser(userId));
    }

    @GetMapping("/api/users/{userId}/trust")
    public ResponseEntity<TrustResponseDto> getUserTrust(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getTrustForUser(userId));
    }

    @GetMapping("/api/users/{userId}/badge/history")
    public ResponseEntity<List<BadgeHistoryDto>> getBadgeHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getBadgeHistory(userId));
    }

    @PostMapping("/api/internal/ratings/publish")
    public ResponseEntity<Map<String, Integer>> publishExpiredWindows(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        int published = ratingPublicationService.publishExpiredWindows();
        return ResponseEntity.ok(Map.of("windowsProcessed", published));
    }
}
