package com.nicolashernandez.financetracker.repository;

import com.nicolashernandez.financetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
