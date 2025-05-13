package com.crewmeister.cmcodingchallenge.unit.service;

import com.crewmeister.cmcodingchallenge.service.ExchangeRateScheduler;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateSchedulerTest {
    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeRateScheduler scheduler;

    @Test
    void scheduledFetchExchangeRate_shouldInvokeServiceMethod() {
        // When
        scheduler.scheduledFetchExchangeRate();

        // Then
        verify(exchangeRateService, times(1)).fetchAndStoreExchangeRatesForAllCurrencies();
    }
}
