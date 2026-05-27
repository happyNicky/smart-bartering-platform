package com.finalyear.liwatch.userprofile;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/createProfile")
    public ResponseEntity<?> createProfile(@RequestBody ProfileRequestDto dto) {
        return profileService.createProfile(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id);
    }

    @GetMapping("/byUserId/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequestDto dto) {
        return profileService.updateProfile(dto);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMe() {
        return profileService.getMyProfile();
    }
}
