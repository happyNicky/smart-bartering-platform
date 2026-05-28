package com.finalyear.liwatch.admin;

/**
 * All possible states for a Post / listing.
 *
 * If your existing Status enum only has ACTIVE / INACTIVE / EXPIRED,
 * add PENDING, FLAGGED, and REMOVED so the admin moderation flow works.
 *
 * ─── Lifecycle ───────────────────────────────────────────────────────────────
 *
 *  [owner creates post]
 *        │
 *        ▼
 *     PENDING  ──(admin approves)──▶  ACTIVE
 *                                        │
 *                         ┌──────────────┤
 *                         ▼              ▼
 *                      FLAGGED       EXPIRED   ◀─ (admin force-expire / TTL)
 *                         │
 *              ┌──────────┴──────────┐
 *              ▼                     ▼
 *           ACTIVE              REMOVED
 *         (approved)          (taken down)
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public enum Status {

    /** Newly created; awaiting admin approval before it goes live. */
    PENDING,

    /** Visible to all users — normal live state. */
    ACTIVE,

    /** Pulled from public view for admin review; not yet removed. */
    FLAGGED,

    /** Permanently taken down by admin or owner; kept for audit purposes. */
    REMOVED,

    /** Listing has passed its natural lifespan (time-based or admin-forced). */
    EXPIRED,

    /** Owner manually closed/deactivated the listing. */
    INACTIVE
}
