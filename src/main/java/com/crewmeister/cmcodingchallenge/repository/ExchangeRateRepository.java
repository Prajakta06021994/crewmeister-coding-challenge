package com.crewmeister.cmcodingchallenge.repository;

import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findByDate(LocalDate date);
    Optional<ExchangeRate> findByCurrencyCodeAndDate(String currencyCode, LocalDate date);
    @Query("SELECT DISTINCT e.currencyCode FROM ExchangeRate e")
    List<String> findDistinctCurrencies();
}

