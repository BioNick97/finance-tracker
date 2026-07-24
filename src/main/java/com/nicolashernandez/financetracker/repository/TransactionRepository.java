package com.nicolashernandez.financetracker.repository;


import com.nicolashernandez.financetracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long>  {
    boolean existsByCategory_Id(Long id);
}
