package com.crewmeister.cmcodingchallenge.integration.service;

import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ResourceNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExchangeRateServiceIntegrationTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @BeforeEach
    void setUp() {
        exchangeRateRepository.deleteAll();
        exchangeRateRepository.save(new ExchangeRate(1L,"USD", new BigDecimal("1.10"), LocalDate.of(2023, 12, 31)));
        exchangeRateRepository.save(new ExchangeRate(2L,"GBP", new BigDecimal("0.85"), LocalDate.of(2023, 12, 31)));
    }

    @Test
    void shouldReturnSupportedCurrencies() {
        List<String> currencies = exchangeRateService.getAllCurrencies();
        assertTrue(currencies.contains("USD"));
        assertTrue(currencies.contains("GBP"));
    }

    @Test
    void shouldReturnExchangeRatesForDate() {
        List<ExchangeRateDTO> rates = exchangeRateService
                .getExchangeRatesForDate(LocalDate.of(2023, 12, 31));
        assertEquals(2, rates.size());
    }

    @Test
    void shouldThrowForFutureDate() {
        LocalDate future = LocalDate.now().plusDays(1);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeRateService.getExchangeRatesForDate(future)
        );
        assertTrue(ex.getMessage().contains("future"));
    }

    @Test
    void shouldReturnExchangeRates() {
        List<ExchangeRateDTO> exchangeRateDTOS = exchangeRateService.getAllExchangeRates();
        assertEquals("USD", exchangeRateDTOS.get(0).getCurrency());
        assertEquals(new BigDecimal("1.10"), exchangeRateDTOS.get(0).getRate());
    }

    @Test
    void shouldThrowIfExchangeRateForGivenDateNotFound() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeRateService.getExchangeRatesForDate(null)
        );
        assertTrue(ex.getMessage().contains("Date must not be null."));
    }

    @Test
    void shouldConvertAmountToEuroCorrectly() {
        ConversionResultDTO result = exchangeRateService.convertAmountToEuro
                (new BigDecimal("110.00"), "USD",
                        LocalDate.of(2023, 12, 31));
        assertEquals("USD", result.getCurrency());
        assertEquals(new BigDecimal("110.00"), result.getOriginalAmount());
        assertEquals(new BigDecimal("100.00"), result.getConvertedToEUR());
    }

    @Test
    void shouldThrowIfCurrencyCodeInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeRateService.convertAmountToEuro(new BigDecimal("100.00"),
                        "XXX", LocalDate.of(2023, 12, 31))
        );
        assertTrue(ex.getMessage().contains("Invalid currency"));
    }

    @Test
    void shouldThrowIfDateInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeRateService.convertAmountToEuro(new BigDecimal("100.00"),
                        "USD", LocalDate.now().plusDays(1))
        );
        assertTrue(ex.getMessage().contains("Date cannot be in the future."));
    }

    @Test
    void shouldThrowIfRateNotFoundForConversion() {
        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                exchangeRateService.convertAmountToEuro(new BigDecimal("100.00"),
                        "USD", LocalDate.of(1990, 1, 1))
        );
        assertTrue(ex.getMessage().contains("Rate not found"));
    }
}
