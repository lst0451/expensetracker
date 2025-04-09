package com.example.expensetracker;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        // Clean the database and add sample data for integration testing
        expenseRepository.deleteAll();
        Expense expense1 = new Expense("Test Expense 1", 10.00, LocalDate.now().minusDays(5), "Travel");
        Expense expense2 = new Expense("Test Expense 2", 20.50, LocalDate.now().minusDays(3), "Food");
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
    }

    @Test
    void testGetExpensesFilteringAndPaging() throws Exception {
        // Test the filtering and pagination functionality by calling GET /api/expenses 
        mockMvc.perform(get("/api/expenses")
                .param("category", "Food")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Expect only 1 record with category Food
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.totalExpenses", is(1)));
    }

    @Test
    void testArchivedExpensesEndpoint() throws Exception {
        // Soft delete (archive) one expense by marking its deleted flag to true.
        Expense expense = expenseRepository.findAll().get(0);
        expense.setDeleted(true);
        expenseRepository.save(expense);

        // Test the new endpoint to get archived expenses.
        mockMvc.perform(get("/api/expenses/archived")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is(expense.getDescription())));
    }
}
