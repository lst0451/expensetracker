package com.example.expensetracker.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // Amount must be greater than 0 (positive) and will be shown with two decimal places in the frontend.
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    // Date must not be in the future
    @PastOrPresent(message = "Date must not be in the future")
    private LocalDate date;

    // Category for filtering and display; fill this field for data testing and demo purposes.
    private String category;

    // Soft delete flag; if true, the expense is archived (soft-deleted)
    @Column(nullable = false)
    private boolean deleted = false;

    // Constructors

    public Expense() {
    }

    public Expense(String description, Double amount, LocalDate date, String category) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.deleted = false;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    // When saving, you may format or round the amount to two decimal places either here or in a service method.
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
