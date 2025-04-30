package com.example.currencyxrates.listeners;

import com.example.currencyxrates.model.ExchangeRate;
import com.example.currencyxrates.repository.ExchangeRateRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExchangeRateListener {

    private ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateListener(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Async
    @RabbitListener(queues = "exchangeRateQueue")
    public void saveExchangeRateToDatabase(ExchangeRate exchangeRate) {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndCreatedAt(
                exchangeRate.getBaseCurrency(), exchangeRate.getTargetCurrency(), exchangeRate.getCreatedAt());
        if (exchangeRateOptional.isEmpty()) {
            exchangeRateRepository.save(exchangeRate);
        }
    }
}
