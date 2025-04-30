package com.example.currencyxrates.exception;

public record ErrorResponse(
        int status,
        String error,
        String timestamp
) {
}
