package com.example.currencyxrates.controller;

import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeRateController {

    private ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateDTO> getExchangeRate(
            @RequestParam String base,
            @RequestParam String target) {
        return ResponseEntity.ok(exchangeRateService.getExchangeRate(base, target));
    }
}
