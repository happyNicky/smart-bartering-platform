package com.finalyear.liwatch.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminStatsResponse {

    // user stats
    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long unverifiedUsers;
    private long newUsersLast30Days;

    // post stats
    private long totalPosts;
    private long activePosts;
    private long removedPosts;
    private long itemPosts;
    private long servicePosts;

    // barter stats
    private long totalBarters;
    private long completedBarters;
    private long activeBarters;

    // report stats
    private long totalReports;
    private long pendingReports;
    private long resolvedReports;

    // community
    private long totalGroups;
    private long activeGroups;

    // badge distribution: LEVEL_1 -> count, etc.
    private Map<String, Long> badgeDistribution;
}
