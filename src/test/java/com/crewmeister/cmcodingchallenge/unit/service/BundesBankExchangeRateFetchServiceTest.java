package com.crewmeister.cmcodingchallenge.unit.service;

import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.strategy.BundesbankExchangeRateFetchStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BundesBankExchangeRateFetchServiceTest {

    @InjectMocks
    private BundesbankExchangeRateFetchStrategy bundesbankStrategy;

    @Mock
    ExchangeRateRepository repository;

    @Test
    void shouldParseAndStoreRatesWhenValidCsv() throws Exception {
        // Test CSV structure
        String mockedCSV =
                "\"Time series\",\"BBEX3.D.USD.EUR.BB.AC.000\"\n" +
                        "\"Currency\",\"USD\"\n" +
                        "\"Unit\",\"1 USD\"\n" +
                        "\"\"\n" +
                        "\"Date\",\"Value\"\n" +
                        "\"2024-01-01\",\"1.1000\"\n" +
                        "\"2024-01-02\",\"1.2000\"\n";

        ReflectionTestUtils.setField(bundesbankStrategy, "tsIdTemplate", "BBEX3.D.%s.EUR.BB.AC.000");
        ReflectionTestUtils.setField(bundesbankStrategy, "urlTemplate", "https://www.bundesbank.de/statistic-rmi/StatisticDownload?tsId=%s&its_csvFormat=en&mode=its");

        // Mock the URL stream
        InputStream mockStream = new ByteArrayInputStream(mockedCSV.getBytes(StandardCharsets.UTF_8));
        BundesbankExchangeRateFetchStrategy spyService = spy(bundesbankStrategy);
        doReturn(mockStream).when(spyService).openUrlStream(anyString());

        // Execute the method under test
        spyService.fetchAndStoreExchangeRates("USD");

        // Verify repository interactions
        ArgumentCaptor<List<ExchangeRate>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(1)).saveAll(captor.capture());

        List<ExchangeRate> savedRates = captor.getValue();
        assertNotNull(savedRates);
        assertEquals(2, savedRates.size());

        // Validate first ExchangeRate object
        ExchangeRate firstRate = savedRates.get(0);
        assertEquals("USD", firstRate.getCurrencyCode());
        assertEquals(LocalDate.of(2024, 1, 1), firstRate.getDate());
        assertEquals(new BigDecimal("1.1000"), firstRate.getExchangeRate());
    }

    @Test
    void shouldNotSaveWhenCurrencyNotSupported() throws Exception {
        // Test CSV with invalid data (no valid rates)
        String mockedCSV =
                "\"Time series\",\"BBEX3.D.USD.EUR.BB.AC.000\"\n" +
                        "\"Currency\",\"USD\"\n" +
                        "\"Unit\",\"1 USD\"\n" +
                        "\"\"\n" +
                        "\"Date\",\"Value\"\n" +
                        "\"2024-01-01\",\"invalidRate\"\n";

        ReflectionTestUtils.setField(bundesbankStrategy, "tsIdTemplate", "BBEX3.D.%s.EUR.BB.AC.000");
        ReflectionTestUtils.setField(bundesbankStrategy, "urlTemplate", "https://www.bundesbank.de/statistic-rmi/StatisticDownload?tsId=%s&its_csvFormat=en&mode=its");

        // Mock the URL stream
        InputStream mockStream = new ByteArrayInputStream(mockedCSV.getBytes(StandardCharsets.UTF_8));
        BundesbankExchangeRateFetchStrategy spyService = spy(bundesbankStrategy);
        doReturn(mockStream).when(spyService).openUrlStream(anyString());

        // Execute the method under test
        spyService.fetchAndStoreExchangeRates("USD");

        // Verify that saveAll is not called due to invalid data
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void shouldLogErrorWhenCsvParsingFails() throws Exception {
        ReflectionTestUtils.setField(bundesbankStrategy, "tsIdTemplate", "BBEX3.D.%s.EUR.BB.AC.000");
        ReflectionTestUtils.setField(bundesbankStrategy, "urlTemplate", "https://www.bundesbank.de/statistic-rmi/StatisticDownload?tsId=%s&its_csvFormat=en&mode=its");

        BundesbankExchangeRateFetchStrategy spyService = spy(bundesbankStrategy);
        doThrow(new IOException("Test IOException")).when(spyService).openUrlStream(anyString());

        // Call method - we're just testing it doesn't throw or crash
        spyService.fetchAndStoreExchangeRates("USD");

        // Optionally verify no saveAll occurred
        verify(repository, never()).saveAll(anyList());
    }
}
