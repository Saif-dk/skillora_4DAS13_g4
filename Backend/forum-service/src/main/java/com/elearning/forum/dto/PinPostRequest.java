package com.elearning.forum.dto;

import jakarta.validation.constraints.NotNull;

public record PinPostRequest(
        @NotNull(message = "pinned field must not be null")
        Boolean pinned
) {
}
