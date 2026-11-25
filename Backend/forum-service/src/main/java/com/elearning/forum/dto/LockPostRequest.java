package com.elearning.forum.dto;

import jakarta.validation.constraints.NotNull;

public record LockPostRequest(
        @NotNull(message = "locked field must not be null")
        Boolean locked
) {
}
