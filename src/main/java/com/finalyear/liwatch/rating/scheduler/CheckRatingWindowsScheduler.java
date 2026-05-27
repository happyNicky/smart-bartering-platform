package com.finalyear.liwatch.rating.scheduler;

import com.finalyear.liwatch.rating.service.RatingPublicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckRatingWindowsScheduler {

    private static final Logger log = LoggerFactory.getLogger(CheckRatingWindowsScheduler.class);

    private final RatingPublicationService ratingPublicationService;

    public CheckRatingWindowsScheduler(RatingPublicationService ratingPublicationService) {
        this.ratingPublicationService = ratingPublicationService;
    }

    @Scheduled(cron = "${liwatch.rating.publish-cron:0 0 1 * * *}")
    public void publishExpiredRatingWindows() {
        int count = ratingPublicationService.publishExpiredWindows();
        log.info("Published {} expired rating window(s)", count);
    }
}
