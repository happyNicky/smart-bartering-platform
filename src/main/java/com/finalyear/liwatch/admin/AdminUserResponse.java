package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.userManagement.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private boolean isVerified;
    private boolean enabled;
    private LocalDateTime createdAt;

    // profile snapshot
    private String location;
    private String bio;
    private String badgeLevel;
    private Double trustScore;
    private String profileImage;

    // activity summary
    private int totalPosts;
    private int totalBarters;
    private int reportCount;

    public static AdminUserResponse from(User user) {
        AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .isVerified(user.isVerified())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt());

        if (user.getUserProfile() != null) {
            builder
                .location(user.getUserProfile().getLocation())
                .bio(user.getUserProfile().getBio())
                .badgeLevel(user.getUserProfile().getBadgeLevel() != null
                        ? user.getUserProfile().getBadgeLevel().name() : null)
                .trustScore(user.getUserProfile().getTrustScore() != null
                        ? user.getUserProfile().getTrustScore().doubleValue() : 0.0)
                .profileImage(user.getUserProfile().getProfileImage());
        }

        return builder.build();
    }
}
