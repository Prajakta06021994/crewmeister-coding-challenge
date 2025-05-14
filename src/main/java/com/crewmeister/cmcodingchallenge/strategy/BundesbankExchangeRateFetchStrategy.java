package com.crewmeister.cmcodingchallenge.strategy;

import com.crewmeister.cmcodingchallenge.constants.Provider;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BundesbankExchangeRateFetchStrategy implements ExchangeRateFetchStrategy{

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Value("${bundesbank.url.template}")
    private String urlTemplate;

    @Value("${bundesbank.tsId.template}")
    private String tsIdTemplate;

    //Logic to fetch and parse CSV from Bundesbank
    @Override
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

    @Override
    public Provider getProvider() {
        return Provider.BUNDESBANK;
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
