package com.crewmeister.cmcodingchallenge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExchangeRateScheduler {
    @Autowired
    private ExchangeRateService exchangeRateService;
    @Scheduled(fixedRate = 60 * 60 * 1000) // every 5 minutes
    public void scheduledFetchExchangeRate() {
        log.info("Running scheduled fetch of exchange rates...");
        exchangeRateService.fetchAndStoreExchangeRatesForAllCurrencies();
    }
}
