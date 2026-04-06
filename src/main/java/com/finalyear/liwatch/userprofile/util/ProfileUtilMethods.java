package com.finalyear.liwatch.userprofile.util;

import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userprofile.ProfileResponseDto;
import com.finalyear.liwatch.userprofile.UserProfile;

public class ProfileUtilMethods {

    public static ProfileResponseDto createResponseDtoFromProfile(UserProfile profile)
    {
        //get user
        User user= profile.getUser();
        UserSummeryDto userSummeryDto= new UserSummeryDto(user.getId(), user.getFullName(), user.getEmail());

        ProfileResponseDto profileResponseDto= new ProfileResponseDto();
        profileResponseDto.setProfileImage(profile.getProfileImage());
        profileResponseDto.setProfileId(profile.getProfileId());
        profileResponseDto.setBio(profile.getBio());
        profileResponseDto.setLocation(profile.getLocation());
        profileResponseDto.setTrustScore(profile.getTrustScore());
        profileResponseDto.setBadgeLevel(profile.getBadgeLevel());
        profileResponseDto.setUser(userSummeryDto);

        return  profileResponseDto;

    }

}
