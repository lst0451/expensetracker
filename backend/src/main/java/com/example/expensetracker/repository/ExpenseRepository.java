package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    // Retrieve only active (non-deleted) expenses
    List<Expense> findByDeletedFalse();

    // Retrieve archived (soft deleted) expenses
    List<Expense> findByDeletedTrue();

    // Retrieve active expenses that are older than the given date (for automatic archiving, etc.)
    List<Expense> findByDeletedFalseAndDateBefore(LocalDate date);
}
