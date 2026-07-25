package com.nicolashernandez.financetracker.controller;

import com.nicolashernandez.financetracker.model.Transaction;
import com.nicolashernandez.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<Transaction> getAllTransactions(
            @RequestParam(name = "month",required = false) YearMonth month,
            @RequestParam(name = "categoryId",required = false) Long categoryId)
    {

        if (month != null && categoryId != null) {
            return transactionService.getTransactionsByMonthAndCategory(
                    month,
                    categoryId
            );
        }

        if (categoryId != null) {
            return transactionService.getTransactionsByCategory(categoryId);
        }


        if (month != null) {
            return transactionService.getTransactionsByMonth(month);
        }
        return transactionService.getAllTransactions();

    }

    @GetMapping("/{id}")
    public Transaction getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(
            @Valid @RequestBody Transaction transaction) {

        return transactionService.createTransaction(transaction);
    }

    @PutMapping("/{id}")
    public Transaction updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody Transaction transaction) {

        return transactionService.updateTransaction(id, transaction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}