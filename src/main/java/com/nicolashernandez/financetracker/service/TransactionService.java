package com.nicolashernandez.financetracker.service;

import com.nicolashernandez.financetracker.model.Transaction;
import com.nicolashernandez.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction createTransaction(Transaction transaction) {
        transaction.setId(null);//Must be null if creating new
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(
            Long id,
            Transaction updatedTransaction
    ) {
        Transaction existingTransaction = getTransaction(id);

        existingTransaction.setDescription(
                updatedTransaction.getDescription()
        );
        existingTransaction.setAmount(
                updatedTransaction.getAmount()
        );

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
                "Transaction not found"
        );
    }

    public void deleteTransaction(Long id) {
        Transaction existingTransaction = getTransaction(id);
        transactionRepository.delete(existingTransaction);
    }
}