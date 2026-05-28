package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.directswap.DirectSwapRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminSwapRequestResponse {

    private Long id;
    private String status;
    private LocalDateTime createdAt;

    // sender (User A)
    private Long senderId;
    private String senderName;
    private String senderEmail;

    // receiver (User B)
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;

    // what is being offered
    private Long offeredPostId;
    private String offeredPostTitle;

    // what is being requested
    private Long requestedPostId;
    private String requestedPostTitle;

    // barter created from this request (null if not yet accepted)
    private Long barterId;

    public static AdminSwapRequestResponse from(DirectSwapRequest r) {
        AdminSwapRequestResponseBuilder b = AdminSwapRequestResponse.builder()
                .id(r.getId())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .createdAt(r.getCreatedAt());

        if (r.getRequestSender() != null) {
            b.senderId(r.getRequestSender().getId())
             .senderName(r.getRequestSender().getFullName())
             .senderEmail(r.getRequestSender().getEmail());
        }
        if (r.getRequestReceiver() != null) {
            b.receiverId(r.getRequestReceiver().getId())
             .receiverName(r.getRequestReceiver().getFullName())
             .receiverEmail(r.getRequestReceiver().getEmail());
        }
        if (r.getOfferedPost() != null) {
            b.offeredPostId(r.getOfferedPost().getPostId())
             .offeredPostTitle(r.getOfferedPost().getTitle());
        }
        if (r.getRequestedPost() != null) {
            b.requestedPostId(r.getRequestedPost().getPostId())
             .requestedPostTitle(r.getRequestedPost().getTitle());
        }
        if (r.getBarter() != null) {
            b.barterId(r.getBarter().getId());
        }

        return b.build();
    }
}
