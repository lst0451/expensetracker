package com.example.expensetracker.config;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ExpenseRepository expenseRepository;

    public DataInitializer(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize data only if there are no expenses in the repository
        if (expenseRepository.count() == 0) {
            List<Expense> expenses = new ArrayList<>();

            // Predefined categories list
            String[] categories = { "Food", "Transportation", "Utilities", "Entertainment", "Health", "Other" };

            for (int i = 0; i < 100; i++) {
                Expense expense = new Expense();

                // Set description
                expense.setDescription("Expense #" + (i + 1));

                // Generate a random amount between 1.0 and 1000.0, then round to two decimal places
                double amount = 1.0 + ThreadLocalRandom.current().nextDouble(999.0);
                amount = Math.round(amount * 100.0) / 100.0;
                expense.setAmount(amount);

                // Randomly choose a category from the list
                int randomCategoryIndex = ThreadLocalRandom.current().nextInt(categories.length);
                expense.setCategory(categories[randomCategoryIndex]);

                // Generate a random date between January 1, 2020 and today (not in the future)
                LocalDate startDate = LocalDate.of(2020, 1, 1);
                long startEpochDay = startDate.toEpochDay();
                long todayEpochDay = LocalDate.now().toEpochDay();
                long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, todayEpochDay + 1);
                LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
                expense.setDate(randomDate);

                expenses.add(expense);
            }

            // Save all expenses into the database
            expenseRepository.saveAll(expenses);
            System.out.println("Initialized " + expenses.size() + " random expenses for testing.");
        }
    }
}
