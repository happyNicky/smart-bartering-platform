package com.finalyear.liwatch.admin;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for barter and swap request oversight.
 *
 * Base path: /api/admin/barters
 *            /api/admin/swap-requests
 *
 * What an admin can do:
 *   - Browse all barters, filter by negotiation status (PENDING / AGREED / CANCELED)
 *   - Drill into a single barter — see both users, both posts, negotiation,
 *     agreements, and the originating swap request
 *   - Force-cancel any barter that is still PENDING (not yet AGREED)
 *   - Browse all swap requests and inspect individual ones
 */
@RestController
@RequiredArgsConstructor
@AdminOnly
public class AdminBarterController {

    private final AdminBarterService barterService;

    // ═════════════════════════════════════════════════════════════════════════
    // BARTERS
    // ═════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/barters
    //   ?negotiationStatus=PENDING&keyword=john&page=0&size=20
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/api/admin/barters")
    public ResponseEntity<AdminApiResponse<List<AdminBarterResponse>>> listBarters(
            @RequestParam(required = false) String negotiationStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                barterService.listBarters(negotiationStatus, keyword, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/barters/user/{userId}
    // All barters where the user is either party
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/api/admin/barters/user/{userId}")
    public ResponseEntity<AdminApiResponse<List<AdminBarterResponse>>> listBartersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(barterService.listBartersByUser(userId, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/barters/{barterId}
    // Full detail: both users, both posts, negotiation, agreements, swap request
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/api/admin/barters/{barterId}")
    public ResponseEntity<AdminApiResponse<AdminBarterResponse>> getBarterDetail(
            @PathVariable Long barterId
    ) {
        return ResponseEntity.ok(barterService.getBarterDetail(barterId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/barters/{barterId}/force-cancel
    //
    // Force-cancels a barter whose negotiation is still PENDING.
    // Also cancels the originating swap request so both posts are freed.
    //
    // Body: { "reason": "Fraudulent listing detected" }
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/api/admin/barters/{barterId}/force-cancel")
    public ResponseEntity<AdminApiResponse<AdminBarterResponse>> forceCancelBarter(
            @PathVariable Long barterId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(barterService.forceCancelBarter(barterId, req));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SWAP REQUESTS
    // ═════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/swap-requests
    //   ?status=PENDING&page=0&size=20
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/api/admin/swap-requests")
    public ResponseEntity<AdminApiResponse<List<AdminSwapRequestResponse>>> listSwapRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(barterService.listSwapRequests(status, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/swap-requests/{requestId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/api/admin/swap-requests/{requestId}")
    public ResponseEntity<AdminApiResponse<AdminSwapRequestResponse>> getSwapRequestDetail(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(barterService.getSwapRequestDetail(requestId));
    }
}
