package com.finalyear.liwatch.userprofile;

import com.finalyear.liwatch.rating.repository.RatingRepository;
import com.finalyear.liwatch.rating.service.RatingService;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import com.finalyear.liwatch.userprofile.util.ProfileUtilMethods;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserUtilService userUtilService;
    private final RatingRepository ratingRepository;
    private final RatingService ratingService;

    public ProfileService(
            ProfileRepository profileRepository,
            UserUtilService userUtilService,
            RatingRepository ratingRepository,
            RatingService ratingService) {
        this.profileRepository = profileRepository;
        this.userUtilService = userUtilService;
        this.ratingRepository = ratingRepository;
        this.ratingService = ratingService;
    }

    public ResponseEntity<?> createProfile(ProfileRequestDto dto) {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        if (profileRepository.findByUser(user).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A profile for this account already exists");
        }
        UserProfile profile = new UserProfile();
        profile.setBio(dto.getBio());
        profile.setProfileImage(dto.getProfileImage());
        profile.setLocation(dto.getLocation());
        profile.setUser(user);
        UserProfile saved = profileRepository.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProfileUtilMethods.createResponseDtoFromProfile(saved, ratingService));
    }

    public ResponseEntity<?> getProfileById(Long id) {
        return profileRepository.findById(id)
                .map(p -> ResponseEntity.ok(ProfileUtilMethods.createResponseDtoFromProfile(p, ratingService)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .<ProfileResponseDto>build());
    }

    public ResponseEntity<?> getProfileByUserId(Long userId) {
        User user = userUtilService.getUserById(userId);
        return profileRepository.findByUser(user)
                .map(p -> ResponseEntity.ok(ProfileUtilMethods.createResponseDtoFromProfile(p, ratingService)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .<ProfileResponseDto>build());
    }

    public ResponseEntity<?> updateProfile(ProfileRequestDto dto) {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        Optional<UserProfile> opt = profileRepository.findByUser(user);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Create a profile first before updating");
        }
        UserProfile profile = opt.get();
        profile.setBio(dto.getBio());
        profile.setLocation(dto.getLocation());
        profile.setProfileImage(dto.getProfileImage());
        UserProfile saved = profileRepository.save(profile);
        return ResponseEntity.ok(ProfileUtilMethods.createResponseDtoFromProfile(saved, ratingService));
    }

    public ResponseEntity<ProfileResponseDto> getMyProfile() {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        UserProfile profile = profileRepository.findByUser(currentUser).orElse(null);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(ProfileUtilMethods.createResponseDtoFromProfile(profile, ratingService));
    }
}
