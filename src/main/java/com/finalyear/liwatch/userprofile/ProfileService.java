package com.finalyear.liwatch.userprofile;

import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.userprofile.util.ProfileUtilMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private UserUtilService userUtilService;

    public ResponseEntity<?> createProfile(ProfileRequestDto profileRequestDto) {

       //get currently authenticated user
        User user= userUtilService.getCurrentlyAuthenticatedUser();
        if(profileRepository.findByUser(user).isPresent()) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body("profile for this account is already created!");
        }

        // create a new userprofile and set the data from profileRequestDto to the newly created userProfile
        UserProfile profile= new UserProfile();
        profile.setBio(profileRequestDto.getBio());
        profile.setProfileImage(profileRequestDto.getProfileImage());
        profile.setUser(user);
        profile.setLocation(profileRequestDto.getLocation());

        // save newly created userProfile to our database
        UserProfile dbProfile=profileRepository.save(profile);

        // create userProfileResponseDto, set the data to it and return it
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProfileUtilMethods.createResponseDtoFromProfile(dbProfile));

    }


    public ResponseEntity<?> getProfileById(Long id) {
        //Get the profile with the given id,If the profile exists or throw an exception.
        Optional<UserProfile> profile;
        if(profileRepository.findById(id).isPresent())
        {
            profile= profileRepository.findById(id);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no profile with id: "+id);
        }

        // convert the profile to profileResponseDto and return
        return ResponseEntity.status(HttpStatus.OK).body( ProfileUtilMethods.createResponseDtoFromProfile(profile.get()));
    }

    public ResponseEntity<?> updateProfile(ProfileRequestDto profileRequestDto) {

        //get currently authenticated user
        User user= userUtilService.getCurrentlyAuthenticatedUser();
        //Get the profile using the user.
        Optional<UserProfile> profile;
        if(profileRepository.findByUser(user).isPresent())
        {
            profile= profileRepository.findByUser(user);
            // update the data
            profile.get().setBio(profileRequestDto.getBio());
            profile.get().setLocation(profileRequestDto.getLocation());
            profile.get().setProfileImage(profileRequestDto.getProfileImage());

            //save the update profile
            UserProfile updatedProfile=profileRepository.save(profile.get());

            // create response dto and return
            return ResponseEntity.status(HttpStatus.OK).body( ProfileUtilMethods.createResponseDtoFromProfile(updatedProfile));
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("create profile first before updating it ");
        }


    }

    public ResponseEntity<ProfileResponseDto> getMyProfile() {
        // 1. Get the exact user from the JWT Token securely
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        UserSummeryDto userSummeryDto= new UserSummeryDto(currentUser.getId(), currentUser.getFullName(), currentUser.getEmail());

        // 2. Safely extract profile data (handling the case where they haven't set up a profile yet)
        ProfileResponseDto profileDto = null;

        if (currentUser.getUserProfile() != null) {

                 profileDto.setBio(currentUser.getUserProfile().getBio());
                 profileDto.setProfileImage(currentUser.getUserProfile().getProfileImage());
                 profileDto.setLocation(currentUser.getUserProfile().getLocation());
                 profileDto.setTrustScore(currentUser.getUserProfile().getTrustScore());
                 profileDto.setBadgeLevel(currentUser.getUserProfile().getBadgeLevel());
                 profileDto.setUser(userSummeryDto);
                 profileDto.setProfileId(currentUser.getUserProfile().getProfileId());

        }


        return ResponseEntity.ok(profileDto);
    }

    public ResponseEntity<?> getProfileByUserId(Long userId) {

        User user= userUtilService.getUserById(userId);

        if( user !=null )
        {
            UserProfile profile= profileRepository.findByUser(user).get();
            return ResponseEntity.status(HttpStatus.OK).body(profile);
        }
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The id "+userId +" is wrong");

    }
}

