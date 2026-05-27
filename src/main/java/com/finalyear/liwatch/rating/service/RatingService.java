package com.finalyear.liwatch.rating.service;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.rating.Rating;
import com.finalyear.liwatch.rating.RatingWindow;
import com.finalyear.liwatch.rating.dto.RatingResponseDto;
import com.finalyear.liwatch.rating.dto.SubmitRatingRequest;
import com.finalyear.liwatch.rating.dto.UpdateRatingRequest;
import com.finalyear.liwatch.rating.repository.RatingRepository;
import com.finalyear.liwatch.review.Review;
import com.finalyear.liwatch.review.ReviewRepository;
import com.finalyear.liwatch.trust.dto.BadgeHistoryDto;
import com.finalyear.liwatch.trust.dto.TrustResponseDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.userbadge.UserBadge;
import com.finalyear.liwatch.userbadge.UserBadgeRepository;
import com.finalyear.liwatch.userprofile.ProfileRepository;
import com.finalyear.liwatch.userprofile.UserProfile;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final RatingWindowService ratingWindowService;
    private final BarterService barterService;
    private final UserUtilService userUtilService;
    private final ProfileRepository profileRepository;
    private final UserBadgeRepository userBadgeRepository;

    public RatingService(
            RatingRepository ratingRepository,
            ReviewRepository reviewRepository,
            RatingWindowService ratingWindowService,
            BarterService barterService,
            UserUtilService userUtilService,
            ProfileRepository profileRepository,
            UserBadgeRepository userBadgeRepository) {
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
        this.ratingWindowService = ratingWindowService;
        this.barterService = barterService;
        this.userUtilService = userUtilService;
        this.profileRepository = profileRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    @Transactional
    public RatingResponseDto submitRating(SubmitRatingRequest request) {
        User fromUser = userUtilService.getCurrentlyAuthenticatedUser();
        Barter barter = barterService.getBarter(request.getBarterId());
        RatingWindow window = ratingWindowService.getOpenWindowForBarter(request.getBarterId());

        if (!barter.getUserA().getId().equals(fromUser.getId())
                && !barter.getUserB().getId().equals(fromUser.getId())) {
            throw new RuntimeException("Only barter participants may submit a rating");
        }

        User toUser = barter.getUserA().getId().equals(fromUser.getId())
                ? barter.getUserB() : barter.getUserA();

        if (fromUser.getId().equals(toUser.getId())) {
            throw new IllegalArgumentException("Self-rating is not allowed");
        }

        if (ratingRepository.findByBarterIdAndFromUserId(request.getBarterId(), fromUser.getId()).isPresent()) {
            throw new IllegalArgumentException("You have already submitted a rating for this barter");
        }

        Rating rating = Rating.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .barter(barter)
                .score(request.getScore())
                .isPublished(false)
                .createdAt(LocalDateTime.now())
                .build();
        rating = ratingRepository.save(rating);

        if (request.getComment() != null && !request.getComment().isBlank()) {
            Review review = Review.builder()
                    .rating(rating)
                    .comment(request.getComment())
                    .isPublished(false)
                    .build();
            reviewRepository.save(review);
        }

        ratingWindowService.markSubmitted(window, fromUser.getId());
        // Blind review: never expose pending rating score/comment via API.
        return pendingDto(rating);
    }

    @Transactional
    public RatingResponseDto updateRating(Long ratingId, UpdateRatingRequest request) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        if (!rating.getFromUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized: you did not submit this rating");
        }
        if (Boolean.TRUE.equals(rating.getIsPublished())) {
            throw new IllegalStateException("Published ratings cannot be edited");
        }

        ratingWindowService.getOpenWindowForBarter(rating.getBarter().getId());

        if (request.getScore() != null) {
            rating.setScore(request.getScore());
            ratingRepository.save(rating);
        }

        Review review = reviewRepository.findByRatingRatingId(ratingId).orElse(null);
        if (request.getComment() != null) {
            if (review == null) {
                review = Review.builder()
                        .rating(rating)
                        .comment(request.getComment())
                        .isPublished(false)
                        .build();
            } else {
                review.setComment(request.getComment());
            }
            reviewRepository.save(review);
        }

        // Blind review: never expose pending rating score/comment via API.
        return pendingDto(rating);
    }

    public List<RatingResponseDto> getPublishedRatingsForUser(Long userId) {
        return ratingRepository.findPublishedByToUserId(userId).stream()
                .map(this::publishedDto)
                .toList();
    }

    public TrustResponseDto getTrustForUser(Long userId) {
        User user = userUtilService.getUserById(userId);
        UserProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found for user " + userId));

        double avg = ratingRepository.averagePublishedScoreForUser(userId);
        BadgeLevel level = profile.getBadgeLevel() != null ? profile.getBadgeLevel() : BadgeLevel.LEVEL_1;

        return TrustResponseDto.builder()
                .userId(userId)
                .trustScore(profile.getTrustScore())
                .badgeLevel(level)
                .badgeLabel(level.getLabel())
                .averageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public List<BadgeHistoryDto> getBadgeHistory(Long userId) {
        return userBadgeRepository.findByUserIdOrderByAwardedAtDesc(userId).stream()
                .map(this::toBadgeHistoryDto)
                .toList();
    }

    private RatingResponseDto publishedDto(Rating rating) {
        String comment = reviewRepository.findByRatingRatingId(rating.getRatingId())
                .map(Review::getComment)
                .orElse(null);
        return RatingResponseDto.builder()
                .ratingId(rating.getRatingId())
                .fromUserId(rating.getFromUser().getId())
                .fromUserName(rating.getFromUser().getFullName())
                .score(rating.getScore())
                .comment(comment)
                .publishedAt(rating.getPublishedAt())
                .build();
    }

    private RatingResponseDto pendingDto(Rating rating) {
        // Intentionally omit score/comment/publishedAt to enforce blind review.
        return RatingResponseDto.builder()
                .ratingId(rating.getRatingId())
                .build();
    }

    private BadgeHistoryDto toBadgeHistoryDto(UserBadge b) {
        return BadgeHistoryDto.builder()
                .badgeId(b.getBadgeId())
                .badgeLevel(b.getBadgeLevel())
                .badgeLabel(b.getBadgeLevel().getLabel())
                .trustScoreAtAward(b.getTrustScoreAtAward())
                .totalSwaps(b.getTotalSwaps())
                .averageRating(b.getAverageRating())
                .reportCount(b.getReportCount())
                .awardedAt(b.getAwardedAt())
                .build();
    }
}
