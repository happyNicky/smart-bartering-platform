package com.finalyear.liwatch.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request body for resolving a report.
 *
 * Extends the base AdminActionRequest with a resolution outcome
 * so the admin can optionally act on the reported user in the same call.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminReportActionRequest extends AdminActionRequest {

    /**
     * What happens to the reported user as a result of this report validation.
     *
     *  NONE      → just mark the report resolved, no action on the user
     *  WARN      → log a warning against the user (future use / notification)
     *  SUSPEND   → suspend the reported user's account immediately
     */
    @NotNull(message = "userAction is required. Use NONE if no user action is needed.")
    private UserAction userAction = UserAction.NONE;

    public enum UserAction {
        NONE,
        WARN,
        SUSPEND
    }
}
