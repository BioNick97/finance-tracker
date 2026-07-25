package com.nicolashernandez.financetracker.service;

import com.nicolashernandez.financetracker.model.Transaction;
import com.nicolashernandez.financetracker.repository.TransactionRepository;
import com.nicolashernandez.financetracker.model.Category;
import com.nicolashernandez.financetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByMonth(YearMonth month) {// YYYY-MM in this case
        LocalDate startDate = month.atDay(1);//At first day of that month and Year
        LocalDate endDate = month.atEndOfMonth();//The last day of that month and year.

        //Calls the Repository Interface. Where things are passed on to the DataBase
        return transactionRepository.findByTransactionDateBetween(
                startDate,
                endDate
        );
    }

    public Transaction createTransaction(Transaction transaction) {
        transaction.setId(null);//Must be null if creating new

        //Verifies the category is valid.
        Category category = getRequiredCategory(transaction);
        transaction.setCategory(category);

        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(
            Long id,
            Transaction updatedTransaction
    ) {
        Transaction existingTransaction = getTransaction(id);

        Category category = getRequiredCategory(updatedTransaction);

        existingTransaction.setDescription(
                updatedTransaction.getDescription()
        );

        existingTransaction.setAmount(
                updatedTransaction.getAmount()
        );

        existingTransaction.setTransactionDate(
                updatedTransaction.getTransactionDate()
        );

        existingTransaction.setCategory(category);

        return transactionRepository.save(existingTransaction);
    }

    public Transaction getTransaction(Long id) {
        Optional<Transaction> result =
                transactionRepository.findById(id);

        if (result.isPresent()) {
            return result.get();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transaction Not Found"
        );
    }

    public void deleteTransaction(Long id) {
        Transaction existingTransaction = getTransaction(id);
        transactionRepository.delete(existingTransaction);
    }

    //Helper Methods
    private Category getRequiredCategory(Transaction transaction) {
        if (transaction.getCategory() == null ||
                transaction.getCategory().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category ID is required"
            );
        }

        Optional<Category> result =
                categoryRepository.findById(
                        transaction.getCategory().getId()
                );

        if (result.isPresent()) {
            return result.get();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Category Not Found"
        );
    }

    public List<Transaction> getTransactionsByMonthAndCategory(YearMonth month, Long categoryId) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        return transactionRepository.findByTransactionDateBetweenAndCategory_Id(
                startDate,
                endDate,
                categoryId
        );


    }

    public List<Transaction> getTransactionsByCategory(Long categoryId) {
        return transactionRepository.findByCategory_Id(categoryId);

    }
}