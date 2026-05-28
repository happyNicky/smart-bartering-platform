package com.finalyear.liwatch.admin;

/**
 * All possible states for a UserReport.
 *
 * If your existing ReportStatus only has PENDING and RESOLVED,
 * add DISMISSED so the dismiss flow works.
 *
 * ─── Lifecycle ────────────────────────────────────────────────────────────
 *
 *  [user files report]
 *         │
 *         ▼
 *      PENDING
 *         │
 *    ┌────┴────┐
 *    ▼         ▼
 * RESOLVED  DISMISSED
 *    │         │
 *    └────┬────┘
 *         ▼
 *      PENDING   ◀─ (admin reopens for re-review)
 *
 * ─────────────────────────────────────────────────────────────────────────
 */
public enum ReportStatus {

    /** Newly filed — awaiting admin review. */
    PENDING,

    /** Admin reviewed and confirmed the report was legitimate. */
    RESOLVED,

    /** Admin reviewed and found the report invalid or unfounded. */
    DISMISSED
}
