package com.elearning.forum.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank(message = "Comment content must not be blank")
        String content
) {
}
