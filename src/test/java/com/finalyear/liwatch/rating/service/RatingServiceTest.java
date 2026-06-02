package com.finalyear.liwatch.rating.service;

import com.finalyear.liwatch.rating.Rating;
import com.finalyear.liwatch.rating.dto.RatingResponseDto;
import com.finalyear.liwatch.rating.dto.SubmitRatingRequest;
import com.finalyear.liwatch.rating.repository.RatingRepository;
import com.finalyear.liwatch.review.ReviewRepository;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository;
import com.finalyear.liwatch.cycleswap.model.CycleBarter;
import com.finalyear.liwatch.userprofile.ProfileRepository;
import com.finalyear.liwatch.userbadge.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RatingWindowService ratingWindowService;

    @Mock
    private BarterService barterService;

    @Mock
    private UserUtilService userUtilService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private CycleBarterRepository cycleBarterRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RatingService ratingService;

    private User currentUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setFullName("Initiator");

        targetUser = new User();
        targetUser.setId(2L);
        targetUser.setFullName("Partner");
    }

    @Test
    void testSubmitCycleRating_Success() {
        // Arrange
        SubmitRatingRequest request = new SubmitRatingRequest();
        request.setCycleBarterId(10L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setComment("Great trade!");

        CycleBarter cycleBarter = new CycleBarter();
        cycleBarter.setId(10L);
        cycleBarter.setUserA(currentUser);
        cycleBarter.setUserB(targetUser);
        
        User userC = new User();
        userC.setId(3L);
        cycleBarter.setUserC(userC);

        when(userUtilService.getCurrentlyAuthenticatedUser()).thenReturn(currentUser);
        when(cycleBarterRepository.findById(10L)).thenReturn(Optional.of(cycleBarter));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RatingResponseDto response = ratingService.submitRating(request);

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getScore());
        assertEquals("Great trade!", response.getComment());
        verify(ratingRepository, times(1)).save(any(Rating.class));
        verify(reviewRepository, times(1)).save(any());
    }
}
