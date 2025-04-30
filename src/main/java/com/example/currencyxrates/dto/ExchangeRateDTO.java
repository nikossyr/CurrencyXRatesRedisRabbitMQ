package com.example.currencyxrates.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExchangeRateDTO {

    String baseCurrency;
    String targetCurrency;
    BigDecimal rate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createdAt;

}
