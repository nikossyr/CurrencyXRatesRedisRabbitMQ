package com.example.currencyxrates.mapper;

import com.example.currencyxrates.dto.ExchangeRateDTO;
import com.example.currencyxrates.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExchangeRateMapper {

    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    //    @Mapping(source = "currency", target = "currencyCode")
    ExchangeRateDTO exchangeRateToExchangeRateDTO(ExchangeRate exchangeRate);

    //    @Mapping(source = "currencyCode", target = "currency")
    ExchangeRate exchangeRateDTOToExchangeRate(ExchangeRateDTO exchangeRateDTO);

}
