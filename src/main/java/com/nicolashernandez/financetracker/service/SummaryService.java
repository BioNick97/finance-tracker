package com.nicolashernandez.financetracker.service;

import com.nicolashernandez.financetracker.dto.MonthlySummary;
import com.nicolashernandez.financetracker.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nicolashernandez.financetracker.model.TransactionType;

import java.time.YearMonth;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final TransactionService transactionService;

    public MonthlySummary getMonthlySummary(YearMonth month){
        List<Transaction> transactions = transactionService.getTransactionsByMonth(month);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        for (Transaction transaction:transactions){
            TransactionType type = transaction.getCategory().getType();

            if(type == TransactionType.INCOME){
                income = income.add(transaction.getAmount());
            }else if(type == TransactionType.EXPENSE){
                expenses = expenses.add(transaction.getAmount());

            }
        }

        BigDecimal balance = income.subtract(expenses);


        return new MonthlySummary(
                month,
                income,
                expenses,
                balance
        );
    }

}
