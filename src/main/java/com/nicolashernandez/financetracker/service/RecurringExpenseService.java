package com.nicolashernandez.financetracker.service;

import com.nicolashernandez.financetracker.model.RecurringExpense;
import com.nicolashernandez.financetracker.repository.CategoryRepository;
import com.nicolashernandez.financetracker.repository.RecurringExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final CategoryRepository categoryRepository;

    public List<RecurringExpense> getAllRecurringExpenses() {
        return recurringExpenseRepository.findAll();
    }

    public RecurringExpense getRecurringExpenseById(Long id) {
        return recurringExpenseRepository.findById(id).orElse(null);
    }

    public RecurringExpense getRecurringExpense(Long id) {
        Optional<RecurringExpense> result =
                recurringExpenseRepository.findById(id);

        if (result.isPresent()) {
            return result.get();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Recurring Expense Not Found"
        );
    }
}
