package com.finalyear.liwatch.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Generic payload for admin actions that need a reason or note.
 * Used for suspend, ban, dismiss report, remove post, etc.
 */
@Data
public class AdminActionRequest {

    /**
     * Human-readable reason for the action (stored in audit log / report).
     * Required so there is always a paper trail.
     */
    @NotBlank(message = "A reason must be provided for this admin action")
    private String reason;

    /**
     * Optional: extra note visible only to other admins.
     */
    private String internalNote;
}
