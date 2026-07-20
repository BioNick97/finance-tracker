package com.nicolashernandez.financetracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nicolashernandez.financetracker.model.Transaction;
import java.math.BigDecimal;
@RestController
public class HelloController {
    @GetMapping
    public String hello(){
        return "Hello, Finance Tracker!";
    }

    @GetMapping("/transaction")
    public Transaction getTransaction() {
        return new Transaction(
                1L,
                "Groceries",
                new BigDecimal("75.50")
        );
    }
}
