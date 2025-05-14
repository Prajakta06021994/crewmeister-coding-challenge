package com.crewmeister.cmcodingchallenge.strategy;

import com.crewmeister.cmcodingchallenge.constants.Provider;

public interface ExchangeRateFetchStrategy {
    /*
        Strategy Pattern implemented to support future external data providers beyond
        Bundesbank, making the system easily extensible.
     */
    public void fetchAndStoreExchangeRates(String currency);
    public Provider getProvider();
}
