package com.finalyear.liwatch.trust;

import com.finalyear.liwatch.Notification.NotificationService;
import com.finalyear.liwatch.barter.barter_managment.BarterRepository;
import com.finalyear.liwatch.rating.repository.RatingRepository;
import com.finalyear.liwatch.report.UserReportRepository;
import com.finalyear.liwatch.report.enums.ReportStatus;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userbadge.UserBadge;
import com.finalyear.liwatch.userbadge.UserBadgeRepository;
import com.finalyear.liwatch.userprofile.UserProfile;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import com.finalyear.liwatch.userprofile.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TrustService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final BarterRepository barterRepository;
    private final UserReportRepository userReportRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final NotificationService notificationService;

    public TrustService(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            RatingRepository ratingRepository,
            BarterRepository barterRepository,
            UserReportRepository userReportRepository,
            UserBadgeRepository userBadgeRepository,
            NotificationService notificationService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.barterRepository = barterRepository;
        this.userReportRepository = userReportRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void recalculate(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double averageRating = ratingRepository.averagePublishedScoreForUser(userId);
        long totalSwaps = barterRepository.countBartersForUser(userId);
        long completedSwaps = barterRepository.countCompletedBartersForUser(userId);
        double swapRate = totalSwaps == 0 ? 0.0 : (double) completedSwaps / totalSwaps;

        double seniorityFactor = seniorityFactor(user);
        long validatedReportCount = userReportRepository.countByReportedUser_IdAndStatus(
                userId, ReportStatus.VALIDATED);

        double raw = (
                (averageRating / 5.0) * 0.50
                        + swapRate * 0.30
                        + seniorityFactor * 0.10
                        - (validatedReportCount * 0.05)
        ) * 5.0;

        BigDecimal trustScore = BigDecimal.valueOf(Math.max(0.0, Math.min(5.0, raw)))
                .setScale(2, RoundingMode.HALF_UP);

        UserProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found for user " + userId));
        profile.setTrustScore(trustScore);
        profileRepository.save(profile);

        deriveBadgeLevel(userId, trustScore, averageRating, (int) totalSwaps, (int) validatedReportCount);
    }

    private double seniorityFactor(User user) {
        LocalDateTime createdAt = user.getCreatedAt();
        if (createdAt == null) {
            return 0.0;
        }
        long months = ChronoUnit.MONTHS.between(createdAt, LocalDateTime.now());
        return Math.min(months / 24.0, 1.0);
    }

    private void deriveBadgeLevel(
            Long userId,
            BigDecimal trustScore,
            double averageRating,
            int totalSwaps,
            int reportCount) {
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = profileRepository.findByUser(user).orElseThrow();

        BadgeLevel newLevel = BadgeLevel.fromTrustScore(trustScore);
        BadgeLevel currentLevel = profile.getBadgeLevel() != null ? profile.getBadgeLevel() : BadgeLevel.LEVEL_1;

        profile.setBadgeLevel(newLevel);
        profileRepository.save(profile);

        if (newLevel == currentLevel) {
            return;
        }

        UserBadge snapshot = UserBadge.builder()
                .userId(userId)
                .badgeLevel(newLevel)
                .trustScoreAtAward(trustScore)
                .totalSwaps(totalSwaps)
                .averageRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP))
                .reportCount(reportCount)
                .awardedAt(LocalDateTime.now())
                .build();
        userBadgeRepository.save(snapshot);

        notificationService.createNotification(
                userId,
                user.getEmail(),
                "Badge level updated",
                "Your badge is now " + newLevel.getLabel() + " (" + newLevel.getLevel() + ").",
                "BADGE_CHANGE"
        );
    }
}
