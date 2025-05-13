package com.crewmeister.cmcodingchallenge.controller;

import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController()
@RequestMapping("/api")
@Validated
public class ExchangeRateController {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @GetMapping("/currencies")
    public ResponseEntity<List<String>> getCurrencies(){
        List<String> currencies = exchangeRateService.getAllCurrencies();
        return new ResponseEntity<>(currencies, HttpStatus.OK);
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<List<ExchangeRateDTO>> getAllExchangeRate(){
        List<ExchangeRateDTO> currencies = exchangeRateService.getAllExchangeRates();
        return new ResponseEntity<>(currencies, HttpStatus.OK);
    }

    @GetMapping("/exchange-rates/{date}")
    public ResponseEntity<List<ExchangeRateDTO>> getRatesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExchangeRateDTO> allRates = exchangeRateService.getExchangeRatesForDate(date);
        return new ResponseEntity<>(allRates, HttpStatus.OK);
    }

    @GetMapping("/convert")
    public ResponseEntity<ConversionResultDTO> convertToEuro(@RequestParam
                                                                 @Pattern(regexp = "[A-Z]{3}", message = "Currency must be 3 uppercase letters")
                                                                 String currency,
                                                             @RequestParam BigDecimal amount,
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
      ConversionResultDTO conversionResultDTO = exchangeRateService.convertAmountToEuro(amount, currency, date);
      return new ResponseEntity<>(conversionResultDTO, HttpStatus.OK);
    }

    @GetMapping("/save")
    public ResponseEntity<String> saveExchangeRate() {
        exchangeRateService.fetchAndStoreExchangeRatesForAllCurrencies();
        return ResponseEntity.ok("Saved to DB.");
    }
}
