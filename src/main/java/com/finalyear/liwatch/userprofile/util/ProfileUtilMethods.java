package com.finalyear.liwatch.userprofile.util;

import com.finalyear.liwatch.rating.service.RatingService;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userprofile.ProfileResponseDto;
import com.finalyear.liwatch.userprofile.UserProfile;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;

public class ProfileUtilMethods {

    public static ProfileResponseDto createResponseDtoFromProfile(UserProfile profile, RatingService ratingService) {
        User user = profile.getUser();
        UserSummeryDto userDto = UserSummeryDto.from(user);

        BadgeLevel level = profile.getBadgeLevel() != null ? profile.getBadgeLevel() : BadgeLevel.LEVEL_1;

        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setProfileId(profile.getProfileId());
        dto.setLocation(profile.getLocation());
        dto.setBio(profile.getBio());
        dto.setTrustScore(profile.getTrustScore());
        dto.setBadgeLevel(level);
        dto.setBadgeLabel(level.getLabel());
        dto.setProfileImage(profile.getProfileImage());
        dto.setUser(userDto);

        if (ratingService != null) {
            var published = ratingService.getPublishedRatingsForUser(user.getId());
            dto.setReviews(published.stream().map(r -> {
                ProfileResponseDto.PublishedReviewDto rev = new ProfileResponseDto.PublishedReviewDto();
                rev.setRatingId(r.getRatingId());
                rev.setFromUserId(r.getFromUserId());
                rev.setFromUserName(r.getFromUserName());
                rev.setFromUserProfileImage(r.getFromUserProfileImage());
                rev.setScore(r.getScore());
                rev.setComment(r.getComment());
                rev.setPublishedAt(r.getPublishedAt());
                return rev;
            }).toList());
        }

        return dto;
    }
}
