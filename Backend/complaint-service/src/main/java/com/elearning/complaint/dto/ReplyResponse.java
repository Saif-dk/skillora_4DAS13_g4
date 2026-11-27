package com.elearning.complaint.dto;

import java.time.Instant;

public record ReplyResponse(
        String id,
        String senderId,
        String senderRole,
        String content,
        Instant createdAt
) {
}
