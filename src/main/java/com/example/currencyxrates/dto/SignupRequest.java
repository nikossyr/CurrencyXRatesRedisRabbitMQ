package com.example.currencyxrates.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignupRequest(
        @Schema(description = "The username of the user", example = "john_doe")
        String username,
        @Schema(description = "The password of the user", example = "strongPassword123")
        String password,
        @Schema(description = "The email address of the user", example = "john.doe@example.com")
        String email) {
}
