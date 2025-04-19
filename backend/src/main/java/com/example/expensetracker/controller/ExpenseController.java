package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@Validated
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // Modified GET /api/expenses interface to support filtering and paging.
    @GetMapping
    public ResponseEntity<?> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Create a PageRequest object using the page and size parameters.
        PageRequest pageable = PageRequest.of(page, size);

        // Get the filtered and paginated list of expenses
        Page<Expense> expensePage = expenseService.getExpenses(category, startDate, endDate, minAmount, maxAmount, pageable);

        // Prepare a response map with the paginated data and related information.
        Map<String, Object> response = new HashMap<>();
        response.put("data", expensePage.getContent());
        response.put("totalPages", expensePage.getTotalPages());
        response.put("totalExpenses", expensePage.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Return a single expense by its id
    @GetMapping("/{id}")
    public Expense getExpenseById(@PathVariable @Min(1) Long id) {
        return expenseService.getExpenseById(id);
    }

    // Create a new expense record with validation
    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody Expense expense) {
        // Additional validation for future dates
        if (expense.getDate() != null && expense.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be in the future");
        }
        
        // Additional validation for amount
        if (expense.getAmount() != null && expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        
        Expense createdExpense = expenseService.createExpense(expense);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    // Update an existing expense record identified by id with validation
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable @Min(1) Long id, @Valid @RequestBody Expense expense) {
        // Additional validation for future dates
        if (expense.getDate() != null && expense.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be in the future");
        }
        
        // Additional validation for amount
        if (expense.getAmount() != null && expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        
        Expense updatedExpense = expenseService.updateExpense(id, expense);
        return new ResponseEntity<>(updatedExpense, HttpStatus.OK);
    }

    // Soft delete (archive) an expense by setting the deleted flag.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable @Min(1) Long id) {
        expenseService.deleteExpense(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // New endpoint to return archived (soft-deleted) expenses
    @GetMapping("/archived")
    public ResponseEntity<?> getArchivedExpenses() {
        List<Expense> archivedExpenses = expenseService.getArchivedExpenses();
        return new ResponseEntity<>(archivedExpenses, HttpStatus.OK);
    }
}
