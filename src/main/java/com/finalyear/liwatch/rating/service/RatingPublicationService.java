package com.finalyear.liwatch.rating.service;

import com.finalyear.liwatch.Notification.NotificationService;
import com.finalyear.liwatch.rating.Rating;
import com.finalyear.liwatch.rating.RatingWindow;
import com.finalyear.liwatch.rating.enums.RatingWindowStatus;
import com.finalyear.liwatch.rating.repository.RatingRepository;
import com.finalyear.liwatch.rating.repository.RatingWindowRepository;
import com.finalyear.liwatch.review.Review;
import com.finalyear.liwatch.review.ReviewRepository;
import com.finalyear.liwatch.trust.TrustService;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RatingPublicationService {

    private final RatingWindowRepository ratingWindowRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final TrustService trustService;
    private final NotificationService notificationService;

    public RatingPublicationService(
            RatingWindowRepository ratingWindowRepository,
            RatingRepository ratingRepository,
            ReviewRepository reviewRepository,
            @Lazy TrustService trustService,
            NotificationService notificationService) {
        this.ratingWindowRepository = ratingWindowRepository;
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
        this.trustService = trustService;
        this.notificationService = notificationService;
    }

    @Transactional
    public int publishExpiredWindows() {
        List<RatingWindow> expired = ratingWindowRepository.findByStatusAndDeadlineBefore(
                RatingWindowStatus.Open, LocalDateTime.now());
        int count = 0;
        for (RatingWindow window : expired) {
            publishWindow(window, RatingWindowStatus.Expired);
            count++;
        }
        return count;
    }

    @Transactional
    public void publishWindow(RatingWindow window, RatingWindowStatus finalStatus) {
        if (window.getStatus() != RatingWindowStatus.Open) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Long barterId = window.getBarter().getId();

        List<Rating> pendingRatings = ratingRepository.findByBarterId(barterId).stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsPublished()))
                .filter(r -> hasSubmitted(window, r.getFromUser().getId()))
                .toList();

        Set<Long> ratedUserIds = new HashSet<>();
        for (Rating rating : pendingRatings) {
            rating.setIsPublished(true);
            rating.setPublishedAt(now);
            ratingRepository.save(rating);

            reviewRepository.findByRatingRatingId(rating.getRatingId()).ifPresent(review -> {
                review.setIsPublished(true);
                review.setPublishedAt(now);
                reviewRepository.save(review);
            });

            ratedUserIds.add(rating.getToUser().getId());
        }

        window.setStatus(finalStatus);
        ratingWindowRepository.save(window);

        for (Long userId : ratedUserIds) {
            trustService.recalculate(userId);
        }

        notifyPublication(window.getUser1(), barterId);
        notifyPublication(window.getUser2(), barterId);
    }

    private boolean hasSubmitted(RatingWindow window, Long fromUserId) {
        if (window.getUser1().getId().equals(fromUserId)) {
            return Boolean.TRUE.equals(window.getUser1Submitted());
        }
        if (window.getUser2().getId().equals(fromUserId)) {
            return Boolean.TRUE.equals(window.getUser2Submitted());
        }
        return false;
    }

    private void notifyPublication(User user, Long barterId) {
        notificationService.createNotification(
                user.getId(),
                user.getEmail(),
                "Ratings published",
                "Ratings for barter #" + barterId + " are now visible on both profiles.",
                "RATING_PUBLISHED"
        );
    }
}
