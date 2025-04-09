package com.example.expensetracker;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService; // Ensure ExpenseService has a constructor-based injection

    private Expense validExpense;
    private Expense invalidExpense; // e.g., with a future date or negative amount

    @BeforeEach
    void setUp() {
        // A valid expense: amount > 0, date not in the future
        validExpense = new Expense("Lunch", 12.50, LocalDate.now().minusDays(1), "Food");

        // An invalid expense: date in the future (or you can also simulate a negative amount scenario)
        invalidExpense = new Expense("Future Expense", 20.00, LocalDate.now().plusDays(1), "Misc");
    }

    @Test
    void testCreateExpenseValid() {
        // When the repository saves a valid expense, return the valid expense object
        when(expenseRepository.save(any(Expense.class))).thenReturn(validExpense);

        Expense created = expenseService.createExpense(validExpense);
        assertNotNull(created);
        assertEquals("Lunch", created.getDescription());
        verify(expenseRepository, times(1)).save(validExpense);
    }

    @Test
    void testCreateExpenseInvalid() {
        // Simulate repository throwing a DataIntegrityViolationException for invalid input
        when(expenseRepository.save(any(Expense.class)))
                .thenThrow(new DataIntegrityViolationException("Invalid expense data"));

        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            expenseService.createExpense(invalidExpense);
        });

        String expectedMessage = "Invalid expense data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        verify(expenseRepository, times(1)).save(invalidExpense);
    }

    @Test
    void testUpdateExpenseValid() {
        Long id = 1L;
        validExpense.setId(id);
        when(expenseRepository.findById(id)).thenReturn(Optional.of(validExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(validExpense);

        // Create an updated expense (without id, service will merge/update based on existing record)
        Expense updatedExpense = new Expense("Dinner", 15.75, LocalDate.now().minusDays(2), "Food");
        Expense result = expenseService.updateExpense(id, updatedExpense);

        assertNotNull(result);
        assertEquals("Dinner", result.getDescription());
        assertEquals(15.75, result.getAmount());
        verify(expenseRepository, times(1)).findById(id);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }
}
