package com.example.currencyxrates.repository;

import com.example.currencyxrates.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndCreatedAt(String baseCurrency, String targetCurrency, LocalDate createdAt);

}
