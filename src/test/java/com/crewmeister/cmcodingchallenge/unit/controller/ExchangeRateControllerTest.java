package com.crewmeister.cmcodingchallenge.unit.controller;


import com.crewmeister.cmcodingchallenge.controller.ExchangeRateController;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.exception.ResourceNotFoundException;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = ExchangeRateController.class)
@AutoConfigureMockMvc
public class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    public void shouldReturnAllExchangeRates() throws Exception {
        // Prepare mock data
        LocalDate date = LocalDate.of(1999, 01, 17);
        ExchangeRateDTO rate1 = new ExchangeRateDTO("USD", BigDecimal.valueOf(1.12), date) ;
        ExchangeRateDTO rate2 = new ExchangeRateDTO("GBP", BigDecimal.valueOf(0.85), date);
        List<ExchangeRateDTO> mockRates = Arrays.asList(rate1, rate2);

        // Mock the service call
        when(exchangeRateService.getAllExchangeRates()).thenReturn(mockRates);

        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/exchange-rates")) // not just /exchange-rates
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenNoExchangeRatesFoundTest() throws Exception {
        when(exchangeRateService.getAllExchangeRates())
                .thenThrow(new ResourceNotFoundException("No currencies found."));

        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnSupportedCurrenciesTest() throws Exception {
        List<String> mockCurrencies = List.of("AUD","BRL");
        when(exchangeRateService.getAllCurrencies()).thenReturn(mockCurrencies);

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenNoCurrenciesFoundTest() throws Exception {
        when(exchangeRateService.getAllCurrencies()).thenThrow(
                new ResourceNotFoundException("No exchange rates found."));

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnExchangeRateByDateTest() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse("1999-01-17", formatter);
        String dateStr = date.format(formatter);
        // Prepare mock data
        ExchangeRateDTO rate1 = new ExchangeRateDTO("USD", BigDecimal.valueOf(1.12), date) ;
        ExchangeRateDTO rate2 = new ExchangeRateDTO("GBP", BigDecimal.valueOf(0.85), date);
        List<ExchangeRateDTO> mockRates = Arrays.asList(rate1, rate2);

        // Mock the service call
        when(exchangeRateService.getExchangeRatesForDate(date)).thenReturn(mockRates);

        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/exchange-rates/" + dateStr))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404NotFoundExchangeRateByDateTest() throws Exception {
        String dateStr = "1999-01-17";
        LocalDate date = LocalDate.parse(dateStr);
        // Mock the service call
        when(exchangeRateService.getExchangeRatesForDate(date)).thenThrow(
                new ResourceNotFoundException("No exchange rates found for date: " + date));

        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/exchange-rates/{date}", dateStr))
                .andExpect(status().isNotFound());
    }

    @Test
    void shoudReturnExceptionIfInvalidDate() throws Exception {
        String invalidDate = "3000-01-01"; // future

        when(exchangeRateService.getExchangeRatesForDate(LocalDate.parse(invalidDate)))
                .thenThrow(new IllegalArgumentException("Date cannot be in the future."));

        mockMvc.perform(get("/api/exchange-rates/{date}", invalidDate))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldConvertToEuroTest() throws Exception {
        String dateStr = "1999-01-17";
        LocalDate date = LocalDate.parse(dateStr);
        String currency = "USD";
        BigDecimal amount = BigDecimal.valueOf(100);
        ConversionResultDTO mockData = new ConversionResultDTO(currency,amount,BigDecimal.valueOf(52.36),date);
        //mock the service call
        when(exchangeRateService.convertAmountToEuro(amount,currency,date)).thenReturn(mockData);
        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/convert/" )
                .param("currency", "USD")
                .param("amount", "100")
                .param("date", "1999-01-17"))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturnExceptionIfInvalidDateWhileConvertToEuroTest() throws Exception {
        String dateStr = "3000-01-17";
        LocalDate date = LocalDate.parse(dateStr);
        String currency = "USD";
        BigDecimal amount = BigDecimal.valueOf(100);
        //mock the service call
        when(exchangeRateService.convertAmountToEuro(amount,currency,date))
                .thenThrow(new IllegalArgumentException("Date cannot be in the future."));;
        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/convert/" )
                        .param("currency", "USD")
                        .param("amount", "100")
                        .param("date", dateStr))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldReturnExceptionIfAmountIsZeroWhileConvertToEuroTest() throws Exception {
        String dateStr = "1999-01-17";
        LocalDate date = LocalDate.parse(dateStr);
        String currency = "USD";
        BigDecimal amount = BigDecimal.ZERO;
        //mock the service call
        when(exchangeRateService.convertAmountToEuro(amount,currency,date))
                .thenThrow(new IllegalArgumentException("Amount is Zero"));;
        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/convert/" )
                        .param("currency", "USD")
                        .param("amount", String.valueOf(amount))
                        .param("date", dateStr))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldReturnExceptionIfInvalidCurrencyWhileConvertToEuroTest() throws Exception {
        String dateStr = "1999-01-17";
        LocalDate date = LocalDate.parse(dateStr);
        String currency = "XXX";
        BigDecimal amount = BigDecimal.TEN;
        //mock the service call
        when(exchangeRateService.convertAmountToEuro(amount,currency,date))
                .thenThrow(new IllegalArgumentException("Invalid Currency."));;
        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/convert/" )
                        .param("currency", "XXX")
                        .param("amount", String.valueOf(amount))
                        .param("date", dateStr))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldThrowExceptionWhenCurrencyRateNotFoundTest() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse("1999-01-17", formatter);
        String currency = "USD";
        BigDecimal amount = BigDecimal.valueOf(100);
        //mock the service call
        when(exchangeRateService.convertAmountToEuro(amount,currency,date))
                .thenThrow(new ResourceNotFoundException("Rate not found for currency " + currency));
        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/convert/" )
                        .param("currency", "USD")
                        .param("amount", "100")
                        .param("date", "1999-01-17"))
                .andExpect(status().isNotFound());

    }

   // @Test
    void shouldThrowExceptionWhenCurrencyRateNotFoundTestd() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse("1999-01-17", formatter);
        String dateStr = date.format(formatter);
        String currency = "XXX";
        BigDecimal amount = BigDecimal.valueOf(100);
        ConversionResultDTO mockData = new ConversionResultDTO(currency,amount,BigDecimal.valueOf(52.36),date);
        //mock the service call
        when(exchangeRateService.convertAmountToEuro(amount,currency,date))
                .thenReturn(mockData);
        // Perform the GET request and assert the response
        mockMvc.perform(get("/api/convert/" )
                        .param("currency", currency)
                        .param("amount", "100")
                        .param("date", dateStr))
                .andExpect(status().isNotFound());

    }

    @Test
    void saveExchangeRateShouldReturnOk() throws Exception {
        // No setup needed since method is void

        mockMvc.perform(get("/api/save"))
                .andExpect(status().isOk())
                .andExpect(content().string("Saved to DB."));

        verify(exchangeRateService, times(1)).fetchAndStoreExchangeRatesForAllCurrencies();
    }
}

