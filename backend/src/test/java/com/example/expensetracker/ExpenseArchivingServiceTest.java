package com.example.expensetracker;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseArchivingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ExpenseArchivingServiceTest {

    @Test
    void testArchiveOldExpenses() {
        // Create mock repository
        ExpenseRepository mockRepository = Mockito.mock(ExpenseRepository.class);
        
        // Create test expenses
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setDeleted(false);
        expense1.setDate(LocalDate.now().minusDays(31));
        
        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setDeleted(false);
        expense2.setDate(LocalDate.now().minusDays(45));
        
        List<Expense> oldExpenses = Arrays.asList(expense1, expense2);
        
        // Set up the mock to return our test expenses
        LocalDate thresholdDate = LocalDate.now().minusDays(30);
        when(mockRepository.findByDeletedFalseAndDateBefore(thresholdDate)).thenReturn(oldExpenses);
        
        // Create the service with the mock repository
        ExpenseArchivingService service = new ExpenseArchivingService(mockRepository);
        
        // Call the method to test
        service.archiveOldExpenses();
        
        // Create a captor to verify the expenses were updated correctly
        ArgumentCaptor<List<Expense>> expenseCaptor = ArgumentCaptor.forClass(List.class);
        
        // Verify that saveAll was called with the updated expenses
        verify(mockRepository).saveAll(expenseCaptor.capture());
        
        // Verify the expenses were updated correctly
        List<Expense> updatedExpenses = expenseCaptor.getValue();
        assertEquals(2, updatedExpenses.size());
        assertTrue(updatedExpenses.get(0).isDeleted());
        assertTrue(updatedExpenses.get(1).isDeleted());
    }
}