package com.elearning.complaint.dto;

import java.time.Instant;
import java.util.List;

public record ComplaintResponse(
        String id,
        String reference,
        String requesterId,
        String subject,
        String description,
        String category,
        String status,
        String assignedAdminId,
        Instant createdAt,
        Instant updatedAt,
        List<ReplyResponse> replies
) {
}
