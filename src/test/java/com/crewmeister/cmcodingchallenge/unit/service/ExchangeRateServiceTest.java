package com.crewmeister.cmcodingchallenge.unit.service;

import com.crewmeister.cmcodingchallenge.config.SupportedCurrenciesConfig;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ResourceNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    SupportedCurrenciesConfig config;

    @Mock
    private ExchangeRateRepository repository;

    @InjectMocks
    private ExchangeRateService rateService;

    @Test
    void shouldReturnAllCurrenciesIfExistsTest(){
        List<String> currencies = List.of("AUD","BRL");
        when(repository.findDistinctCurrencies()).thenReturn(currencies);
        List<String> result = rateService.getAllCurrencies();
        assertEquals(currencies, result);
    }

    @Test
    void shouldReturnErrorIfAllCurrenciesAreEmptyTest(){
        List<String> currencies = List.of("","");
        when(repository.findDistinctCurrencies()).thenReturn(currencies);
        assertThrows(ResourceNotFoundException.class, ()->rateService.getAllCurrencies());
    }

    @Test
    void shouldReturnAllRatesTest(){
        LocalDate date = LocalDate.of(1999, 01, 17);
        ExchangeRate usdRate = new ExchangeRate(1L,"USD", BigDecimal.valueOf(1.91),date);
        ExchangeRate audRate = new ExchangeRate(2L,"AUD", BigDecimal.valueOf(1.51),date);
        List<ExchangeRate> rates = Arrays.asList(usdRate, audRate);
        when(repository.findAll()).thenReturn(rates);
        List<ExchangeRateDTO> expected = Arrays.asList(
                new ExchangeRateDTO("USD", BigDecimal.valueOf(1.91), date),
                new ExchangeRateDTO("AUD", BigDecimal.valueOf(1.51), date)
        );
        List<ExchangeRateDTO> result = rateService.getAllExchangeRates();
        assertEquals(expected,result);
    }

    @Test
    void shouldRThrowExceptionIfNoExchangeRateTest(){
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(ResourceNotFoundException.class,()->rateService.getAllExchangeRates());
    }


    @Test
    void shouldReturnAllRatesIfWithInProvidedDateTest(){
        LocalDate date = LocalDate.of(1999, 01, 17);
        ExchangeRate usdRate = new ExchangeRate(1L,"USD", BigDecimal.valueOf(1.91),date);
        ExchangeRate audRate = new ExchangeRate(2L,"AUD", BigDecimal.valueOf(1.51),date);
        List<ExchangeRate> rates = Arrays.asList(usdRate, audRate);
        when(repository.findByDate(date)).thenReturn(rates);
        List<ExchangeRateDTO> expected = Arrays.asList(
                new ExchangeRateDTO("USD", BigDecimal.valueOf(1.91), date),
                new ExchangeRateDTO("AUD", BigDecimal.valueOf(1.51), date)
        );
        List<ExchangeRateDTO> result = rateService.getExchangeRatesForDate(date);
        assertEquals(expected,result);
    }

    @Test
    void shouldThrowExceptionIfNoExchangeRateToGivenDateTest(){
        LocalDate date = LocalDate.of(1999, 01, 17);
        when(repository.findByDate(date)).thenReturn(Collections.emptyList());
        assertThrows(ResourceNotFoundException.class,()->rateService.getExchangeRatesForDate(date));
    }

    @Test
    void shouldThrowExceptionIfDateIsNull(){
        LocalDate date = null;

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rateService.getExchangeRatesForDate(date)
        );
        assertEquals("Date must not be null.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionIfDateIsInFuture(){
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rateService.getExchangeRatesForDate(futureDate)
        );
        assertEquals("Date cannot be in the future.", ex.getMessage());
    }

    @Test
    void shouldReturnAmountConvertedToEuroForAGivenAmountCurrencyAndDateTest(){
        String currency = "USD";
        LocalDate date = LocalDate.of(1999, 01, 17);
        BigDecimal amount = BigDecimal.valueOf(100);
        ExchangeRate usdRate = new ExchangeRate(1L,"USD", BigDecimal.valueOf(1.91),date);
        when(repository.findByCurrencyCodeAndDate(currency,date)).thenReturn(Optional.of(usdRate));
        when(config.getCurrencies()).thenReturn(List.of("USD", "EUR", "GBP"));
        ConversionResultDTO expected = new ConversionResultDTO(currency,amount,BigDecimal.valueOf(52.36),date);
        ConversionResultDTO result = rateService.convertAmountToEuro(amount,currency,date);
        assertEquals(expected,result);
    }

    @Test
    void shouldReturnExceptionIfAmountIsZero(){
        String currency = "USD";
        LocalDate date = LocalDate.of(1999, 01, 17);
        Exception ex  = assertThrows(IllegalArgumentException.class, ()->
                rateService.convertAmountToEuro(BigDecimal.ZERO,
                        currency, date));
        assertEquals("Amount must be greater than zero.", ex.getMessage());
    }

    @Test
    void shouldReturnExceptionIfCurrencyIsNull() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rateService.convertAmountToEuro(BigDecimal.TEN, null, LocalDate.now())
        );
        assertEquals("Currency must not be null or blank.", ex.getMessage());
    }

    @Test
    void shouldReturnExceptionIfDateIsFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rateService.convertAmountToEuro(BigDecimal.TEN, "USD", futureDate)
        );
        assertEquals("Date cannot be in the future.", ex.getMessage());
    }

    @Test
    void shoudReturnExceptionIfCurrencyIsInvalid() {
        when(config.getCurrencies()).thenReturn(List.of("USD", "EUR", "GBP"));

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rateService.convertAmountToEuro(BigDecimal.TEN, "XXX", LocalDate.now())
        );

        assertEquals("Invalid currency code.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionIfExchangeRateNotExistForGivenAmountCurrencyAndDateTest(){
        LocalDate date = LocalDate.of(1999, 01, 17);
        String currency = "USD";
        BigDecimal amount = BigDecimal.valueOf(100);
        when(config.getCurrencies()).thenReturn(List.of("USD", "EUR", "GBP"));
        when(repository.findByCurrencyCodeAndDate(currency,date)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->rateService.convertAmountToEuro(amount,currency,date));
    }

}
