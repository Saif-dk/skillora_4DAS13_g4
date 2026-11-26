package com.elearning.forum.dto;

import java.time.Instant;

public record CommentResponse(
        String id,
        String postId,
        String content,
        String authorId,
        String authorEmail,
        String authorRole,
        Instant createdAt,
        Instant updatedAt
) {
}
