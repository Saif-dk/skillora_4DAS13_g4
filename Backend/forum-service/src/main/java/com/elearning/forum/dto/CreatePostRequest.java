package com.elearning.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @NotBlank(message = "Content must not be blank")
        String content,

        @NotBlank(message = "Category ID must not be blank")
        String categoryId
) {
}
