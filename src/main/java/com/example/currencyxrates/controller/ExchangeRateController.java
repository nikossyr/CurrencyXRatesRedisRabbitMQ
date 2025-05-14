package com.example.currencyxrates.controller;

import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange")
@Tag(name = "Exchange Rate", description = "Operations for fetching and managing exchange rates")
@SecurityRequirement(name = "bearerAuth")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }
    @Operation(
            summary = "Get exchange rate between two currencies",
            description = "Returns the latest exchange rate from base to target currency"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal error")
    })
    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateDTO> getExchangeRate(
            @RequestParam String base,
            @RequestParam String target) {
        return ResponseEntity.ok(exchangeRateService.getExchangeRate(base, target));
    }
}
