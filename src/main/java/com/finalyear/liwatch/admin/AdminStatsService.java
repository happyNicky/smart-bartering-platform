package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.community_group.CommunityGroup;
import com.finalyear.liwatch.community_group.CommunityGroupRepository;
import com.finalyear.liwatch.report.enums.ReportStatus;
import com.finalyear.liwatch.userManagement.utils.enums.Status;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AdminUserRepository userRepo;
    private final AdminPostRepository postRepo;
    private final AdminReportRepository reportRepo;
    private final AdminBarterRepository barterRepo;
    private final CommunityGroupRepository groupRepo;

    @Transactional(readOnly = true)
    public AdminApiResponse<AdminStatsResponse> getStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Fetch user stats
        long totalUsers = userRepo.count();
        long activeUsers = userRepo.countByStatus(Status.ACTIVE);
        long suspendedUsers = userRepo.countByStatus(Status.SUSPENDED);
        long unverifiedUsers = userRepo.countByIsVerified(false);
        long newUsers = userRepo.countByCreatedAtAfter(thirtyDaysAgo);

        // Fetch post stats
        long totalPosts = postRepo.count();
        long activePosts = postRepo.countByStatus(com.finalyear.liwatch.Post.enums.Status.ACTIVE);
        long removedPosts = postRepo.countByStatus(com.finalyear.liwatch.Post.enums.Status.CLOSED); // CLOSED status as inactive/proxy
        long itemPosts = postRepo.countItems();
        long servicePosts = postRepo.countServices();

        // Fetch barter stats
        long totalBarters = barterRepo.count();
        long completedBarters = barterRepo.countCompleted();
        long activeBarters = barterRepo.countActive();

        // Fetch report stats
        long totalReports = reportRepo.count();
        long pendingReports = reportRepo.countByStatus(ReportStatus.PENDING);
        long resolvedReports = reportRepo.countByStatus(ReportStatus.VALIDATED) + reportRepo.countByStatus(ReportStatus.REJECTED);

        // Fetch community group stats
        long totalGroups = groupRepo.count();
        long activeGroups = groupRepo.findAll().stream()
                .filter(g -> g.getStatus() == CommunityGroup.Status.ACTIVE)
                .count();

        // Fetch badge distribution
        Map<String, Long> badgeDist = new HashMap<>();
        for (BadgeLevel level : BadgeLevel.values()) {
            badgeDist.put(level.name(), 0L);
        }
        userRepo.findAll().forEach(u -> {
            if (u.getUserProfile() != null && u.getUserProfile().getBadgeLevel() != null) {
                String badge = u.getUserProfile().getBadgeLevel().name();
                badgeDist.put(badge, badgeDist.getOrDefault(badge, 0L) + 1);
            }
        });

        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .suspendedUsers(suspendedUsers)
                .unverifiedUsers(unverifiedUsers)
                .newUsersLast30Days(newUsers)
                .totalPosts(totalPosts)
                .activePosts(activePosts)
                .removedPosts(removedPosts)
                .itemPosts(itemPosts)
                .servicePosts(servicePosts)
                .totalBarters(totalBarters)
                .completedBarters(completedBarters)
                .activeBarters(activeBarters)
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .resolvedReports(resolvedReports)
                .totalGroups(totalGroups)
                .activeGroups(activeGroups)
                .badgeDistribution(badgeDist)
                .build();

        return AdminApiResponse.ok(stats);
    }
}
