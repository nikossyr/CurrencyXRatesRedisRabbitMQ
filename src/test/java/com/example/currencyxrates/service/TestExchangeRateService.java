package com.example.currencyxrates.service;

import com.example.currencyxrates.config.RabbitMQProperties;
import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.dto.ExchangeRateResponseDTO;
import com.example.currencyxrates.model.ExchangeRate;
import com.example.currencyxrates.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestExchangeRateService {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ExchangeRateService exchangeRateService;


    @BeforeEach
    void setUp() throws Exception {
        Field redisCacheName = ExchangeRateService.class.getDeclaredField("redisCacheName");
        redisCacheName.setAccessible(true);
        redisCacheName.set(exchangeRateService, "exchangeRates");

        RabbitMQProperties rabbitMQProperties = new RabbitMQProperties();
        rabbitMQProperties.setExchange("exchange");
        rabbitMQProperties.setRoutingKey("routing.key");
        Field rabbitMQPropertiesField = ExchangeRateService.class.getDeclaredField("rabbitMQProperties");
        rabbitMQPropertiesField.setAccessible(true);
        rabbitMQPropertiesField.set(exchangeRateService, rabbitMQProperties);
    }

    @Test
    public void getExchangeRate_cachedValue() {
        Cache cache = mock(Cache.class);
        ExchangeRateDTO cachedValue = new ExchangeRateDTO();
        cachedValue.setBaseCurrency("USD");
        cachedValue.setTargetCurrency("EUR");
        cachedValue.setRate(BigDecimal.valueOf(1.1));
        cachedValue.setCreatedAt(LocalDate.now());

        when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        when(cache.get("USD_EUR", ExchangeRateDTO.class)).thenReturn(cachedValue);

        ExchangeRateDTO result = exchangeRateService.getExchangeRate("USD", "EUR");

        assertEquals(cachedValue, result);
        verifyNoInteractions(exchangeRateRepository, restTemplate);
    }

    @Test
    void getExchangeRate_valueInDB() {
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        when(cache.get("USD_EUR", ExchangeRateDTO.class)).thenReturn(null);

        ExchangeRate entity = new ExchangeRate();
        entity.setId(1);
        entity.setBaseCurrency("USD");
        entity.setTargetCurrency("EUR");
        entity.setRate(BigDecimal.valueOf(1.44));
        entity.setCreatedAt(LocalDate.now());
        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndCreatedAt("USD", "EUR", LocalDate.now()))
                .thenReturn(Optional.of(entity));

        ExchangeRateDTO result = exchangeRateService.getExchangeRate("USD", "EUR");

        assertEquals("USD", result.getBaseCurrency());
        assertEquals("EUR", result.getTargetCurrency());
        assertEquals(BigDecimal.valueOf(1.44), result.getRate());

        verify(cache).put(eq("USD_EUR"), any());
        verify(exchangeRateRepository).findByBaseCurrencyAndTargetCurrencyAndCreatedAt(any(), any(), any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getExchangeRate_valueFromApi() {
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        when(cache.get(eq("USD_EUR"), eq(ExchangeRateDTO.class))).thenReturn(null);
        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndCreatedAt("USD", "EUR", LocalDate.now()))
                .thenReturn(Optional.empty());

        ResponseEntity<ExchangeRateResponseDTO> apiResponse = mock(ResponseEntity.class);
        ExchangeRateResponseDTO exchangeRateResponseDTO = new ExchangeRateResponseDTO();
        exchangeRateResponseDTO.setSuccess(true);
        exchangeRateResponseDTO.setSource("USD");
        exchangeRateResponseDTO.setQuotes(Map.of(
                "EUR", BigDecimal.valueOf(1.25),
                "CAD", BigDecimal.valueOf(2.25),
                "DIN", BigDecimal.valueOf(0.25)));
        when(apiResponse.getBody()).thenReturn(exchangeRateResponseDTO);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(ExchangeRateResponseDTO.class))).thenReturn(apiResponse);

        ExchangeRateDTO result = exchangeRateService.getExchangeRate("USD", "EUR");

        assertEquals("USD", result.getBaseCurrency());
        assertEquals("EUR", result.getTargetCurrency());
        assertEquals(BigDecimal.valueOf(1.25), result.getRate());
        assertEquals(LocalDate.now(), result.getCreatedAt());

        verify(exchangeRateRepository).findByBaseCurrencyAndTargetCurrencyAndCreatedAt(eq("USD"), eq("EUR"), eq(LocalDate.now()));
        verify(cache).put(eq("USD_EUR"), any(ExchangeRateDTO.class));
        verify(cache).put(eq("USD_CAD"), any(ExchangeRateDTO.class));
        verify(cache).put(eq("USD_DIN"), any(ExchangeRateDTO.class));

        verify(rabbitTemplate, times(3)).convertAndSend(
                eq("exchange"),
                eq("routing.key"),
                any(ExchangeRate.class),
                any(MessagePostProcessor.class)
        );
    }

}
