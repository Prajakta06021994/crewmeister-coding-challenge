package com.crewmeister.cmcodingchallenge.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Data
public class ConversionResultDTO {
    private String currency;
    private BigDecimal originalAmount;
    private BigDecimal convertedToEUR;
    private LocalDate date;

    public ConversionResultDTO(String currency, BigDecimal originalAmount, BigDecimal convertedToEUR, LocalDate date) {
        this.currency = currency;
        this.originalAmount = originalAmount;
        this.convertedToEUR = convertedToEUR;
        this.date = date;
    }

}
