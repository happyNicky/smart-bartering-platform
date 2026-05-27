package com.finalyear.liwatch.rating.service;

import com.finalyear.liwatch.Notification.NotificationService;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.rating.RatingWindow;
import com.finalyear.liwatch.rating.enums.RatingWindowStatus;
import com.finalyear.liwatch.rating.repository.RatingWindowRepository;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RatingWindowService {

    private static final int WINDOW_DAYS = 7;

    private final RatingWindowRepository ratingWindowRepository;
    private final BarterService barterService;
    private final NotificationService notificationService;
    private final RatingPublicationService ratingPublicationService;

    public RatingWindowService(
            RatingWindowRepository ratingWindowRepository,
            BarterService barterService,
            NotificationService notificationService,
            RatingPublicationService ratingPublicationService) {
        this.ratingWindowRepository = ratingWindowRepository;
        this.barterService = barterService;
        this.notificationService = notificationService;
        this.ratingPublicationService = ratingPublicationService;
    }

    @Transactional
    public RatingWindow openWindowForBarter(Long barterId) {
        return ratingWindowRepository.findByBarterId(barterId).orElseGet(() -> {
            Barter barter = barterService.getBarter(barterId);
            LocalDateTime deadline = LocalDateTime.now().plusDays(WINDOW_DAYS);

            RatingWindow window = RatingWindow.builder()
                    .barter(barter)
                    .user1(barter.getUserA())
                    .user2(barter.getUserB())
                    .user1Submitted(false)
                    .user2Submitted(false)
                    .deadline(deadline)
                    .status(RatingWindowStatus.Open)
                    .build();

            RatingWindow saved = ratingWindowRepository.save(window);
            notifyWindowOpened(barter.getUserA(), barterId, deadline);
            notifyWindowOpened(barter.getUserB(), barterId, deadline);
            return saved;
        });
    }

    @Transactional
    public void markSubmitted(RatingWindow window, Long submittingUserId) {
        if (window.getUser1().getId().equals(submittingUserId)) {
            window.setUser1Submitted(true);
        } else if (window.getUser2().getId().equals(submittingUserId)) {
            window.setUser2Submitted(true);
        } else {
            throw new RuntimeException("User is not a participant in this rating window");
        }
        ratingWindowRepository.save(window);

        if (Boolean.TRUE.equals(window.getUser1Submitted()) && Boolean.TRUE.equals(window.getUser2Submitted())) {
            ratingPublicationService.publishWindow(window, RatingWindowStatus.Published);
        }
    }

    public RatingWindow getOpenWindowForBarter(Long barterId) {
        RatingWindow window = ratingWindowRepository.findByBarterId(barterId)
                .orElseThrow(() -> new RuntimeException("No rating window open for barter #" + barterId));
        if (window.getStatus() != RatingWindowStatus.Open) {
            throw new RuntimeException("Rating window for barter #" + barterId + " is no longer open");
        }
        if (window.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Rating window for barter #" + barterId + " has expired");
        }
        return window;
    }

    private void notifyWindowOpened(User user, Long barterId, LocalDateTime deadline) {
        notificationService.createNotification(
                user.getId(),
                user.getEmail(),
                "Rate your swap partner",
                "You can rate your partner for barter #" + barterId
                        + ". Window closes at " + deadline + ".",
                "RATING_WINDOW_OPEN"
        );
    }
}
