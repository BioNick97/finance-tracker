package com.nicolashernandez.financetracker.repository;


import com.nicolashernandez.financetracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>  {
    boolean existsByCategory_Id(Long id);

    List<Transaction> findByTransactionDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByCategory_Id(Long categoryId);

    List<Transaction> findByTransactionDateBetweenAndCategory_Id(LocalDate startDate, LocalDate endDate, Long categoryId);
}
