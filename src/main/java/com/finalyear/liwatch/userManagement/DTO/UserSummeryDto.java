package com.finalyear.liwatch.userManagement.DTO;

import com.finalyear.liwatch.userManagement.model.User;
import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class UserSummeryDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String profileImage;
    private String badgeLevel;

    public UserSummeryDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UserSummeryDto(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public UserSummeryDto(Long id, String name, String email, String role, String profileImage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImage = profileImage;
    }

    public UserSummeryDto(Long id, String name, String email, String role, String profileImage, String badgeLevel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImage = profileImage;
        this.badgeLevel = badgeLevel;
    }

    public static UserSummeryDto from(User user) {
        if (user == null) {
            return null;
        }
        UserSummeryDto dto = new UserSummeryDto();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setProfileImage(user.getUserProfile() != null ? user.getUserProfile().getProfileImage() : null);
        dto.setBadgeLevel(user.getUserProfile() != null && user.getUserProfile().getBadgeLevel() != null ? user.getUserProfile().getBadgeLevel().name() : null);
        return dto;
    }
}
