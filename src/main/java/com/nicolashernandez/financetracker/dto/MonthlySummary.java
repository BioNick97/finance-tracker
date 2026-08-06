package com.nicolashernandez.financetracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummary(
        YearMonth month,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {

}

