package com.nicolashernandez.financetracker.controller;

import com.nicolashernandez.financetracker.service.SummaryService;
import com.nicolashernandez.financetracker.dto.MonthlySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {
    private final SummaryService summaryService;

    @GetMapping
    public MonthlySummary getMonthlySummary(
            @RequestParam(name = "month") YearMonth month
    ) {
        return summaryService.getMonthlySummary(month);
    }


}
