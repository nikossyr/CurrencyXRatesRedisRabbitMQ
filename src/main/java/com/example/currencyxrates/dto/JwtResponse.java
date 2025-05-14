package com.example.currencyxrates.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record JwtResponse(
        @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR...")
        String token
) {
}
