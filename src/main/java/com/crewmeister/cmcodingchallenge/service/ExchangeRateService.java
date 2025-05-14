package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.config.SupportedCurrenciesConfig;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ResourceNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private RestTemplate restTemplate;

    @Value("${bundesbank.url.template}")
    private String urlTemplate;

    @Value("${bundesbank.tsId.template}")
    private String tsIdTemplate;

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

    // Logic to fetch and parse CSV from Bundesbank
    @Transactional
    public void fetchAndStoreExchangeRates(String currency) {
        var tsId = buildTsId(currency);
        var url = buildUrl(tsId);

        log.info("Fetching rates for currency: {}", currency);

        try (InputStream inputStream = openUrlStream(url);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<String[]> dataLines = reader.lines()
                    .skip(5) // Skip header/meta
                    .map(line -> line.replace("\"", "").split("\t|,"))
                    .filter(parts -> parts.length >= 2)
                    .collect(Collectors.toList());

            if (dataLines.stream().noneMatch(parts -> isValidRate(parts[1]))) {
                log.warn("Currency {} is not supported based on rate data", currency);
                return;
            }

            List<ExchangeRate> rates = dataLines.stream()
                    .map(parts -> parseExchangeRate(parts, currency))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!rates.isEmpty()) {
                exchangeRateRepository.saveAll(rates);
                log.info("Saved {} rates for currency {}", rates.size(), currency);
            } else {
                log.warn("No valid exchange rates found for currency {}", currency);
            }

        } catch (IOException e) {
            log.error("Failed to download or parse CSV for {}: {}", currency, e.getMessage());
        }
    }

    private ExchangeRate parseExchangeRate(String[] parts, String currency) {
        try {
            LocalDate date = LocalDate.parse(parts[0].trim());
            BigDecimal rate = parseRate(parts[1].trim());

            ExchangeRate exchangeRate = new ExchangeRate();
            exchangeRate.setCurrencyCode(currency);
            exchangeRate.setDate(date);
            exchangeRate.setExchangeRate(rate);

            return exchangeRate;
        } catch (Exception e) {
            log.warn("Skipping malformed line: {} — {}", Arrays.toString(parts), e.getMessage());
            return null;
        }
    }

    public InputStream openUrlStream(String url) throws IOException {
        return new URL(url).openStream();
    }

    private String buildTsId(String currency) {
        return String.format(tsIdTemplate, currency);
    }

    private String buildUrl(String tsId) {
        return String.format(urlTemplate, tsId);
    }

    private BigDecimal parseRate(String rateStr) {
        return (rateStr == null || rateStr.isBlank() || rateStr.equals(".") || rateStr.equals("-"))
                ? BigDecimal.ZERO
                : new BigDecimal(rateStr.replace(",", "."));
    }

    private boolean isValidRate(String rateStr) {
        return rateStr != null && !rateStr.isBlank() && !rateStr.equals(".") && !rateStr.equals("-");
    }
}

