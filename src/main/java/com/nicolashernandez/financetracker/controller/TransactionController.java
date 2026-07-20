package com.nicolashernandez.financetracker.controller;

import com.nicolashernandez.financetracker.model.Transaction;
import com.nicolashernandez.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();

    }
    @GetMapping("/{id}")
    public Transaction getTransaction(
            @PathVariable("id") Long id){

        return transactionService.getTransaction(id);

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(
            @Valid @RequestBody Transaction transaction){
     return transactionService.createTransaction(transaction);
    }

    @PutMapping("/{id}")
    public Transaction updateTransaction(
             @PathVariable("id") Long id,
             @Valid @RequestBody Transaction transaction){

        return transactionService.updateTransaction(id, transaction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(
            @PathVariable("id") Long id) {

        transactionService.deleteTransaction(id);
    }

}