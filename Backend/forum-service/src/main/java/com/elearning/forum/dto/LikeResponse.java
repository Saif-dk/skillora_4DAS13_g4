package com.elearning.forum.dto;

public record LikeResponse(
        boolean liked,
        int likesCount
) {
}
