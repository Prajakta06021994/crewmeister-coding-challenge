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

    @PostConstruct
    public void runOnStartup() {
        exchangeRateService.fetchAndStoreExchangeRatesForAllCurrencies();
    }

    @Scheduled(cron = "0 0 6 ? * MON")  // weekly on Monday at 6AM
    public void scheduledFetchExchangeRate() {
        log.info("Running scheduled fetch of exchange rates...");
        exchangeRateService.fetchAndStoreExchangeRatesForAllCurrencies();
    }
}
