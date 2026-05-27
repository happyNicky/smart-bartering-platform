package com.finalyear.liwatch.trust;

import com.finalyear.liwatch.userprofile.ProfileRepository;
import com.finalyear.liwatch.userprofile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrustSeniorityScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrustSeniorityScheduler.class);

    private final ProfileRepository profileRepository;
    private final TrustService trustService;

    public TrustSeniorityScheduler(ProfileRepository profileRepository, TrustService trustService) {
        this.profileRepository = profileRepository;
        this.trustService = trustService;
    }

    @Scheduled(cron = "${liwatch.trust.seniority-cron:0 0 2 * * *}")
    public void recalculateSeniorityDrift() {
        List<UserProfile> profiles = profileRepository.findAll();
        for (UserProfile profile : profiles) {
            if (profile.getUser() != null) {
                trustService.recalculate(profile.getUser().getId());
            }
        }
        log.info("Seniority trust recalculation completed for {} profiles", profiles.size());
    }
}
