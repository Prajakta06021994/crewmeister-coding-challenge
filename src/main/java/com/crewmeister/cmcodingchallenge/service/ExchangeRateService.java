package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.config.SupportedCurrenciesConfig;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.exception.ResourceNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.strategy.ExchangeRateFetchStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExchangeRateService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private SupportedCurrenciesConfig config;

    @Autowired
    private List<ExchangeRateFetchStrategy> strategies;

    public List<String> getAllCurrencies(){
        List<String> currencies = exchangeRateRepository.findDistinctCurrencies()
                .stream()
                .filter(currency -> currency != null && !currency.isBlank())
                .collect(Collectors.toList());

        return Optional.of(currencies)
                .filter(currency -> !currency.isEmpty())
                .orElseThrow(()-> new ResourceNotFoundException("No currencies found."));
    }

    public List<ExchangeRateDTO> getAllExchangeRates() {
        return Optional.of(exchangeRateRepository.findAll())
                .filter(rate -> !rate.isEmpty())
                .orElseThrow(()->new
                ResourceNotFoundException("No exchange rates found."))
                .stream()
                .map(e-> new ExchangeRateDTO(e.getCurrencyCode(),e.getExchangeRate(),e.getDate()))
                .collect(Collectors.toList());
    }

    public List<ExchangeRateDTO> getExchangeRatesForDate(LocalDate date) {
        Optional.ofNullable(date)
                .orElseThrow(() -> new IllegalArgumentException("Date must not be null."));

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future.");
        }
        return Optional.ofNullable(exchangeRateRepository.findByDate(date))
                .filter(rate -> !rate.isEmpty())
                .orElseThrow(() -> new
                        ResourceNotFoundException("No exchange rates found for date: " + date))
                .stream()
                .map(e -> new ExchangeRateDTO(e.getCurrencyCode(), e.getExchangeRate(), e.getDate()))
                .collect(Collectors.toList());
    }

    public ConversionResultDTO convertAmountToEuro(BigDecimal amount, String currency, LocalDate date) {
        Optional.ofNullable(amount)
                .filter(a -> a.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new IllegalArgumentException("Amount must be greater than zero."));

        Optional.ofNullable(currency)
                .filter(c -> !c.trim().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Currency must not be null or blank."));

        Optional.ofNullable(date)
                .orElseThrow(() -> new IllegalArgumentException("Date must not be null."));

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future.");
        }
        if(!config.getCurrencies().contains(currency.trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid currency code.");
        }
        return exchangeRateRepository.findByCurrencyCodeAndDate(
                currency.trim().toUpperCase(), date)
                .map(rate -> {
                    BigDecimal euro = amount.divide(rate.getExchangeRate(), 2, RoundingMode.HALF_DOWN);
                    return new ConversionResultDTO(rate.getCurrencyCode(), amount, euro, date);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Rate not found for currency " + currency + " on " + date));
    }

    public void fetchAndStoreExchangeRatesForAllCurrencies() {
        config.getCurrencies().stream()
                .filter(code -> !"EUR".equalsIgnoreCase(code))
                .forEach(this::fetchAndStoreExchangeRates);
    }

    public void fetchAndStoreExchangeRates(String currency) {
        for (ExchangeRateFetchStrategy strategy : strategies) {
            try {
                strategy.fetchAndStoreExchangeRates(currency);
                log.info("Saved rates for currency for the provider {} ", strategy.getProvider());
                return;
            } catch (Exception e) {
                log.warn("Strategy failed for provider {} with error {}", strategy.getProvider(),
                        e.getMessage());
            }
        }
        log.error("No provider could fetch rates for currency: {}", currency);
    }
}

