package com.example.currencyxrates.mapper;

import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExchangeRateMapper {

    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    ExchangeRateDTO exchangeRateToExchangeRateDTO(ExchangeRate exchangeRate);

    ExchangeRate exchangeRateDTOToExchangeRate(ExchangeRateDTO exchangeRateDTO);

}
