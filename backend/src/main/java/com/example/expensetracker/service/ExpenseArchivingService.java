package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseArchivingService {

    private final ExpenseRepository expenseRepository;

    public ExpenseArchivingService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // This scheduled method will run every day at midnight.
    @Scheduled(cron = "0 0 0 * * ?")
    public void archiveOldExpenses() {
        LocalDate thresholdDate = LocalDate.now().minusDays(30);
        // Query for expenses not already archived and with a date before the threshold.
        List<Expense> expensesToArchive = expenseRepository.findByDeletedFalseAndDateBefore(thresholdDate);
        expensesToArchive.forEach(expense -> expense.setDeleted(true));
        expenseRepository.saveAll(expensesToArchive);
    }
}
