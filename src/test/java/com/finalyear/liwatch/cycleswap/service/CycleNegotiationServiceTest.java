package com.finalyear.liwatch.cycleswap.service;

import com.finalyear.liwatch.cycleswap.dto.CycleNegotiationResponseDto;
import com.finalyear.liwatch.cycleswap.model.CycleAgreement;
import com.finalyear.liwatch.cycleswap.model.CycleBarter;
import com.finalyear.liwatch.cycleswap.model.CycleNegotiation;
import com.finalyear.liwatch.cycleswap.repository.CycleAgreementRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleNegotiationRepository;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CycleNegotiationServiceTest {

    @Mock
    private CycleNegotiationRepository cycleNegotiationRepository;

    @Mock
    private CycleBarterRepository cycleBarterRepository;

    @Mock
    private CycleAgreementRepository cycleAgreementRepository;

    @Mock
    private UserUtilService userUtilService;

    @InjectMocks
    private CycleNegotiationService cycleNegotiationService;

    private User currentUser;
    private CycleBarter cycleBarter;
    private CycleAgreement agreement;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);

        User userB = new User();
        userB.setId(2L);
        
        User userC = new User();
        userC.setId(3L);

        cycleBarter = new CycleBarter();
        cycleBarter.setId(100L);
        cycleBarter.setUserA(currentUser);
        cycleBarter.setUserB(userB);
        cycleBarter.setUserC(userC);

        CycleNegotiation negotiation = new CycleNegotiation();
        negotiation.setId(200L);
        negotiation.setCycleBarter(cycleBarter);
        cycleBarter.setCycleNegotiation(negotiation);

        agreement = new CycleAgreement();
        agreement.setCycleBarter(cycleBarter);
    }

    @Test
    void testSignAgreement_Success() {
        // Arrange
        when(userUtilService.getCurrentlyAuthenticatedUser()).thenReturn(currentUser);
        when(cycleBarterRepository.findById(100L)).thenReturn(Optional.of(cycleBarter));
        when(cycleAgreementRepository.findAll()).thenReturn(java.util.List.of(agreement));

        // Act
        String result = cycleNegotiationService.signAgreement(100L);

        // Assert
        assertEquals("Signed successfully", result);
        assertTrue(agreement.isUserASigned());
        verify(cycleAgreementRepository, times(1)).save(agreement);
    }
}
