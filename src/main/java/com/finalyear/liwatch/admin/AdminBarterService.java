package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBarterService {

    private final AdminBarterRepository      barterRepo;
    private final AdminDirectSwapRepository  swapRepo;

    // ── List all barters ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminBarterResponse>> listBarters(
            String negotiationStatus,
            String keyword,
            Pageable pageable) {

        Page<AdminBarterResponse> mapped = barterRepo
                .searchBarters(negotiationStatus, keyword, pageable)
                .map(barter -> AdminBarterResponse.from(barter));

        return AdminApiResponse.ofPage(mapped);
    }

    // ── All barters involving a specific user ─────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminBarterResponse>> listBartersByUser(
            Long userId, Pageable pageable) {

        Page<AdminBarterResponse> mapped = barterRepo
                .findByUserId(userId, pageable)
                .map(barter -> AdminBarterResponse.from(barter));

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Single barter detail ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<AdminBarterResponse> getBarterDetail(Long barterId) {
        Barter barter = findBarterOrThrow(barterId);
        return AdminApiResponse.ok(AdminBarterResponse.from(barter));
    }

    // ── Force-cancel a barter ─────────────────────────────────────────────────

    /**
     * Admin intervention: cancel an active barter and its negotiation.
     * Only bartes whose negotiation is still PENDING can be force-cancelled —
     * once AGREED the exchange is considered done.
     */
    @Transactional
    public AdminApiResponse<AdminBarterResponse> forceCancelBarter(
            Long barterId, AdminActionRequest req) {

        Barter barter = findBarterOrThrow(barterId);

        if (barter.getNegotiation() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This barter has no negotiation attached.");
        }

        if (barter.getNegotiation().getStatus() == NegotiationStatus.AGREED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel a barter that has already been agreed upon.");
        }

        if (barter.getNegotiation().getStatus() == NegotiationStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Barter is already cancelled.");
        }

        // cancel the negotiation
        barter.getNegotiation().setStatus(NegotiationStatus.CANCELED);

        // cancel the originating swap request too so the posts are freed
        if (barter.getSwapRequest() != null) {
            barter.getSwapRequest().setStatus(RequestStatus.CANCELED);
        }

        barterRepo.save(barter);

        log.warn("[ADMIN] Force-cancelled barter {} (userA: {}, userB: {}) — reason: {}",
                barterId,
                barter.getUserA() != null ? barter.getUserA().getEmail() : "?",
                barter.getUserB() != null ? barter.getUserB().getEmail() : "?",
                req.getReason());

        return AdminApiResponse.ok(
                AdminBarterResponse.from(barter),
                "Barter force-cancelled successfully.");
    }

    // ── List all swap requests ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminSwapRequestResponse>> listSwapRequests(
            String status,
            Pageable pageable) {

        Page<AdminSwapRequestResponse> mapped = swapRepo
                .searchRequests(status, pageable)
                .map(r -> AdminSwapRequestResponse.from(r));

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Single swap request detail ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<AdminSwapRequestResponse> getSwapRequestDetail(Long requestId) {
        DirectSwapRequest request = swapRepo.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Swap request not found: id=" + requestId));

        return AdminApiResponse.ok(AdminSwapRequestResponse.from(request));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Barter findBarterOrThrow(Long barterId) {
        return barterRepo.findByIdWithDetails(barterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Barter not found: id=" + barterId));
    }
}
