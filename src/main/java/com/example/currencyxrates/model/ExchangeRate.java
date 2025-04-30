package com.example.currencyxrates.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "EXCHANGE_RATE")
@Getter
@Setter
@ToString
public class ExchangeRate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    long id;

    @Column(length = 3, nullable = false)
    String baseCurrency;
    @Column(length = 3, nullable = false)
    String targetCurrency;
    BigDecimal rate;

    LocalDate createdAt;

}
