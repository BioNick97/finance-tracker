package com.nicolashernandez.financetracker.repository;

import com.nicolashernandez.financetracker.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long>  {

}
