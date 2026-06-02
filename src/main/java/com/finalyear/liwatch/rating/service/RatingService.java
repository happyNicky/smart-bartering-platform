package com.finalyear.liwatch.rating.service;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.rating.Rating;
import com.finalyear.liwatch.rating.RatingWindow;
import com.finalyear.liwatch.rating.dto.RatingResponseDto;
import com.finalyear.liwatch.rating.dto.RatingWindowDto;
import com.finalyear.liwatch.rating.dto.SubmitRatingRequest;
import com.finalyear.liwatch.rating.dto.UpdateRatingRequest;
import com.finalyear.liwatch.rating.enums.RatingWindowStatus;
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
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

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
    private final com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository cycleBarterRepository;
    private final JdbcTemplate jdbcTemplate;

    public RatingService(
            RatingRepository ratingRepository,
            ReviewRepository reviewRepository,
            RatingWindowService ratingWindowService,
            BarterService barterService,
            UserUtilService userUtilService,
            ProfileRepository profileRepository,
            UserBadgeRepository userBadgeRepository,
            com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository cycleBarterRepository,
            JdbcTemplate jdbcTemplate) {
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
        this.ratingWindowService = ratingWindowService;
        this.barterService = barterService;
        this.userUtilService = userUtilService;
        this.profileRepository = profileRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.cycleBarterRepository = cycleBarterRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE ratings MODIFY barter_id BIGINT NULL");
        } catch (Exception e) {
            // Ignore if already applied or user lacks permissions
        }
    }

    @Transactional
    public RatingResponseDto submitRating(SubmitRatingRequest request) {
        User fromUser = userUtilService.getCurrentlyAuthenticatedUser();

        if (request.getCycleBarterId() != null) {
            return submitCycleRating(request, fromUser);
        }

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

    private RatingResponseDto submitCycleRating(SubmitRatingRequest request, User fromUser) {
        com.finalyear.liwatch.cycleswap.model.CycleBarter cycleBarter = cycleBarterRepository.findById(request.getCycleBarterId())
                .orElseThrow(() -> new RuntimeException("Cycle Barter not found"));

        if (!cycleBarter.getUserA().getId().equals(fromUser.getId())
                && !cycleBarter.getUserB().getId().equals(fromUser.getId())
                && !cycleBarter.getUserC().getId().equals(fromUser.getId())) {
            throw new RuntimeException("Only cycle barter participants may submit a rating");
        }

        User toUser = userUtilService.getUserById(request.getToUserId());
        if (fromUser.getId().equals(toUser.getId())) {
            throw new IllegalArgumentException("Self-rating is not allowed");
        }

        Rating rating = Rating.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .cycleBarter(cycleBarter)
                .score(request.getScore())
                .isPublished(true) // For simplicity, cycle ratings are published immediately
                .createdAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();
        rating = ratingRepository.save(rating);

        if (request.getComment() != null && !request.getComment().isBlank()) {
            Review review = Review.builder()
                    .rating(rating)
                    .comment(request.getComment())
                    .isPublished(true)
                    .publishedAt(LocalDateTime.now())
                    .build();
            reviewRepository.save(review);
        }

        return publishedDto(rating);
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

    public List<RatingResponseDto> getCycleBarterRatings(Long cycleBarterId) {
        return ratingRepository.findAll().stream()
                .filter(r -> r.getCycleBarter() != null && r.getCycleBarter().getId().equals(cycleBarterId))
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
        String profileImage = rating.getFromUser().getUserProfile() != null
                ? rating.getFromUser().getUserProfile().getProfileImage()
                : null;
        return RatingResponseDto.builder()
                .ratingId(rating.getRatingId())
                .fromUserId(rating.getFromUser().getId())
                .fromUserName(rating.getFromUser().getFullName())
                .fromUserProfileImage(profileImage)
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

    @Transactional(readOnly = true)
    public RatingWindowDto getRatingWindowDetails(Long barterId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        java.util.Optional<RatingWindow> windowOpt = ratingWindowService.findWindowByBarterId(barterId);

        if (windowOpt.isEmpty()) {
            return RatingWindowDto.builder()
                    .status(null)
                    .build();
        }

        RatingWindow window = windowOpt.get();

        boolean isUser1 = window.getUser1().getId().equals(currentUser.getId());
        boolean isUser2 = window.getUser2().getId().equals(currentUser.getId());

        if (!isUser1 && !isUser2) {
            throw new RuntimeException("Unauthorized: you are not a participant in this barter");
        }

        boolean userSubmitted = isUser1 ? Boolean.TRUE.equals(window.getUser1Submitted()) : Boolean.TRUE.equals(window.getUser2Submitted());
        boolean otherUserSubmitted = isUser1 ? Boolean.TRUE.equals(window.getUser2Submitted()) : Boolean.TRUE.equals(window.getUser1Submitted());

        RatingWindowDto.RatingWindowDtoBuilder builder = RatingWindowDto.builder()
                .windowId(window.getWindowId())
                .status(window.getStatus().name())
                .deadline(window.getDeadline())
                .userSubmitted(userSubmitted)
                .otherUserSubmitted(otherUserSubmitted);

        // Fetch current user's rating
        java.util.Optional<Rating> myRatingOpt = ratingRepository.findByBarterIdAndFromUserId(barterId, currentUser.getId());
        if (myRatingOpt.isPresent()) {
            Rating myRating = myRatingOpt.get();
            builder.myRatingId(myRating.getRatingId());
            builder.myScore(myRating.getScore());
            reviewRepository.findByRatingRatingId(myRating.getRatingId()).ifPresent(review -> {
                builder.myComment(review.getComment());
            });
        }

        // Fetch partner's rating (only if window is Published or Expired)
        if (window.getStatus() == RatingWindowStatus.Published || window.getStatus() == RatingWindowStatus.Expired) {
            Long partnerId = isUser1 ? window.getUser2().getId() : window.getUser1().getId();
            java.util.Optional<Rating> partnerRatingOpt = ratingRepository.findByBarterIdAndFromUserId(barterId, partnerId);
            if (partnerRatingOpt.isPresent()) {
                Rating partnerRating = partnerRatingOpt.get();
                if (Boolean.TRUE.equals(partnerRating.getIsPublished())) {
                    builder.partnerScore(partnerRating.getScore());
                    reviewRepository.findByRatingRatingId(partnerRating.getRatingId()).ifPresent(review -> {
                        builder.partnerComment(review.getComment());
                    });
                }
            }
        }

        return builder.build();
    }
}
