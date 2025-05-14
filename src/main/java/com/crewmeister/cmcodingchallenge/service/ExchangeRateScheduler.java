package com.crewmeister.cmcodingchallenge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class ExchangeRateScheduler {

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**At application startup, this method automatically fetches
       exchange rates and stores them in the database. */
    @PostConstruct
    public void runOnStartup() {
        exchangeRateService.fetchAndStoreExchangeRatesForAllCurrencies();
    }

    /**
     * Scheduled task that runs automatically at application startup.
     *
     * It fetches historical foreign exchange rates (with EUR as the base currency)
     * from the Bundesbank public API and stores them in the H2 in-memory database.
     *
     * This ensures that the system has initial data available for currency conversion
     * and exchange rate lookup.
     */

    @Scheduled(cron = "0 0 6 ? * MON")  // weekly on Monday at 6AM
    public void scheduledFetchExchangeRate() {
        log.info("Running scheduled fetch of exchange rates...");
        exchangeRateService.fetchAndStoreExchangeRatesForAllCurrencies();
    }
}
