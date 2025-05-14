package com.crewmeister.cmcodingchallenge.integration.controller;


import com.crewmeister.cmcodingchallenge.config.SupportedCurrenciesConfig;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ExchangeRateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private SupportedCurrenciesConfig config;

    @BeforeEach
    void setup() {
        // Clear and seed test data
        exchangeRateRepository.deleteAll();

        exchangeRateRepository.save(
                new ExchangeRate(1L,"USD", new BigDecimal("1.10"),
                        LocalDate.of(2023, 12, 31))
        );
    }

    @Test
    void shouldReturnSupportedCurrencies() throws Exception {
        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnExchangeRatesByDate() throws Exception {
        mockMvc.perform(get("/api/exchange-rates/2023-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldConvertToEuroShouldReturnConversionResult() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("currency", "USD")
                        .param("amount", "110.00")
                        .param("date", "2023-12-31"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnNotFoundForMissingExchangeRatesByDate() throws Exception {
        mockMvc.perform(get("/api/exchange-rates/1990-01-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnExchangeRateForCurrencyOnDate() throws Exception {
        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldConvertToEuroInvalidCurrencyShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("currency", "XXX")
                        .param("amount", "100.00")
                        .param("date", "2023-12-31"))
                .andExpect(status().isBadRequest());
                //.andExpect(content().string(containsString("Invalid currency code")));
    }

    @Test
    void shouldConvertToEuroRateNotFoundShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("currency", "USD")
                        .param("amount", "100.00")
                        .param("date", "1990-01-01")) // no data seeded
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Rate not found")));
    }

    //@Test
    void saveExchangeRate_shouldStoreRatesInDatabase() throws Exception {
        mockMvc.perform(get("/api/save"))
                .andExpect(status().isOk())
                .andExpect(content().string("Saved to DB."));

        // Example: verify that at least one exchange rate was saved
        assertFalse(exchangeRateRepository.findAll().isEmpty());
    }

    //@Test
    void saveExchangeRate_whenServiceFails_shouldReturnServerError() throws Exception {
        doThrow(new RuntimeException("Failed")).when(exchangeRateService)
                .fetchAndStoreExchangeRatesForAllCurrencies();

        mockMvc.perform(get("/api/save"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed")));
    }

}
