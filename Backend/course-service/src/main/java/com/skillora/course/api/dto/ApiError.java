package com.skillora.course.api.dto;

import java.time.Instant;

public record ApiError(Instant instant, int status, String error, String message, String path) {
}
