package com.elearning.forum.dto;

import java.time.Instant;

public record PostResponse(
        String id,
        String title,
        String content,
        String categoryId,
        String categoryName,
        String authorId,
        String authorEmail,
        String authorRole,
        boolean pinned,
        boolean locked,
        int likesCount,
        boolean likedByMe,
        int commentCount,
        Instant createdAt,
        Instant updatedAt
) {
}
