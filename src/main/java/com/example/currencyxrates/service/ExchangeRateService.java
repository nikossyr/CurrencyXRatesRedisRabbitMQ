package com.example.currencyxrates.service;

import com.example.currencyxrates.config.RabbitMQProperties;
import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.dto.ExchangeRateResponseDTO;
import com.example.currencyxrates.exception.CacheNotFoundException;
import com.example.currencyxrates.mapper.ExchangeRateMapper;
import com.example.currencyxrates.model.ExchangeRate;
import com.example.currencyxrates.repository.ExchangeRateRepository;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private static final String EXCHANGE_API_URL = "https://api.exchangerate.host/live?access_key=%s&source=%s&currencies=%s";

    private final RestTemplate restTemplate;

    @Value("${spring.currency-exchange.api.key}")
    private String exchangeApiKey;

    @Value("${spring.redis.name}")
    private String redisCacheName;

//    private RedisTemplate<String, ExchangeRateDTO> redisTemplate;

    private RabbitTemplate rabbitTemplate;

    private RabbitMQProperties rabbitMQProperties;

    private ExchangeRateRepository exchangeRateRepository;

    private CacheManager cacheManager;

    public ExchangeRateService(RestTemplate restTemplate,
                               RabbitTemplate rabbitTemplate, RabbitMQProperties rabbitMQProperties,
                               ExchangeRateRepository exchangeRateRepository, CacheManager cacheManager) {
        this.restTemplate = restTemplate;
//        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQProperties = rabbitMQProperties;
        this.exchangeRateRepository = exchangeRateRepository;
        this.cacheManager = cacheManager;
    }

    public ExchangeRateDTO getExchangeRate(String base, String target) {

        String cacheKey = base + "_" + target;
        ExchangeRateDTO exchangeRateDTO = getFromCache(cacheKey);

        if (exchangeRateDTO != null) {
            return exchangeRateDTO;
        }

        Optional<ExchangeRate> exchangeRateOptional = exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndCreatedAt(base, target, LocalDate.now());
        if (exchangeRateOptional.isPresent()) {
            ExchangeRate persistedExchangeRate = exchangeRateOptional.get();
            ExchangeRateDTO persistedExchangeRateDTO = ExchangeRateMapper.INSTANCE.exchangeRateToExchangeRateDTO(persistedExchangeRate);
            addToCache(cacheKey, persistedExchangeRateDTO);
            return persistedExchangeRateDTO;
        }

        Map<String, ExchangeRateDTO> exchangeRates = fetchBaseCurrencyExchangeRatesFromAPI(base);
        exchangeRates.forEach((key, value) -> {
            String currentCacheKey = base + "_" + key;
            addToCache(currentCacheKey, value);
            sendExchangeRateToQueue(ExchangeRateMapper.INSTANCE.exchangeRateDTOToExchangeRate(value));
        });

        return exchangeRates.get(target);
    }

    void addToCache(String cacheKey, ExchangeRateDTO persistedExchangeRateDTO) {
        Cache cache = cacheManager.getCache(redisCacheName);
        if (cache == null) {
            throw new CacheNotFoundException("Cache '" + redisCacheName + "' has not been created or is unavailable.");
        }
        cache.put(cacheKey, persistedExchangeRateDTO);

    }

    ExchangeRateDTO getFromCache(String cacheKey) {
        Cache cache = cacheManager.getCache(redisCacheName);
        if (cache == null) {
            throw new CacheNotFoundException("Cache '" + redisCacheName + "' has not been created or is unavailable.");
        }
        return cache.get(cacheKey, ExchangeRateDTO.class);
    }

//    void addToCache(String cacheKey, ExchangeRateDTO persistedExchangeRateDTO) {
//        redisTemplate.opsForValue().set(cacheKey, persistedExchangeRateDTO, Duration.ofMinutes(30));
//    }

    Map<String, ExchangeRateDTO> fetchBaseCurrencyExchangeRatesFromAPI(String base) {
        String url = String.format(EXCHANGE_API_URL, exchangeApiKey, base, "");
        ResponseEntity<ExchangeRateResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, null, ExchangeRateResponseDTO.class);
        ExchangeRateResponseDTO exchangeRateResponseDTO = response.getBody();
        assert exchangeRateResponseDTO != null;
        Map<String, BigDecimal> ratesWithBaseCurrencyPrefix = exchangeRateResponseDTO.getQuotes();
        Map<String, ExchangeRateDTO> exchangeRatesMap = new HashMap<>();
        ratesWithBaseCurrencyPrefix.forEach((key, rate) -> {
            String targetCurrency = key.replaceFirst("^" + base, "");
            ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
            exchangeRateDTO.setBaseCurrency(base);
            exchangeRateDTO.setTargetCurrency(targetCurrency);
            exchangeRateDTO.setRate(rate);
            exchangeRateDTO.setCreatedAt(LocalDate.now());
            exchangeRatesMap.put(targetCurrency, exchangeRateDTO);
        });
        return exchangeRatesMap;
    }

    void sendExchangeRateToQueue(ExchangeRate exchangeRate) {
        rabbitTemplate.convertAndSend(rabbitMQProperties.getExchange(), rabbitMQProperties.getRoutingKey(), exchangeRate, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }
}
