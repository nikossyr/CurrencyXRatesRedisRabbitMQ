package com.example.currencyxrates.dto;

import com.example.currencyxrates.util.UnixToLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
public class ExchangeRateResponseDTO {

    Boolean success;
    String terms;
    String privacy;
    @JsonDeserialize(using = UnixToLocalDateDeserializer.class)
    LocalDate timestamp;
    private String source;
    private String date;
    private Map<String, BigDecimal> quotes = new HashMap<>();
}
