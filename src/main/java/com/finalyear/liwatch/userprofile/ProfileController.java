package com.finalyear.liwatch.userprofile;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/profile")
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileService profileService;

    @PostMapping("/createProfile")
    public ResponseEntity<?> createProfile(@RequestBody ProfileRequestDto profileRequestDto)
    {

        return profileService.createProfile(profileRequestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id)
    {

        return profileService.getProfileById(id);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequestDto profileRequestDto)
    {
        return  profileService.updateProfile(profileRequestDto);
    }
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMe() {
        return profileService.getMyProfile();
    }
}
