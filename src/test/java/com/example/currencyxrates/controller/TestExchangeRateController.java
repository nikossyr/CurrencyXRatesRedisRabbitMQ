package com.example.currencyxrates.controller;

import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
public class TestExchangeRateController {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ExchangeRateService service;

    @Test
    void shouldReturnExchangeRate() throws Exception {
        // Given
        ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
        exchangeRateDTO.setRate(new BigDecimal("13.555"));
        given(service.getExchangeRate("USD", "EUR")).willReturn(exchangeRateDTO);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/exchange/rate?base=USD&target=EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").exists());
    }
}
