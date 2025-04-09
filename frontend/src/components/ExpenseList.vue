<template>
  <div class="expense-list">
    <h2>Expense List</h2>

    <!-- Filter Controls -->
    <div class="filter-controls">
      <label>
        Description:
        <input v-model="filters.description" placeholder="Expense Description" />
      </label>

      <label>
        Category:
        <select v-model="filters.category">
          <option value="">All Categories</option>
          <option value="Food">Food</option>
          <option value="Travel">Travel</option>
          <option value="Shopping">Shopping</option>
          <!-- You can add more categories as needed -->
        </select>
      </label>

      <label>
        Start Date:
        <input type="date" v-model="filters.startDate" />
      </label>

      <label>
        End Date:
        <input type="date" v-model="filters.endDate" />
      </label>

      <label>
        Min Amount:
        <input type="number" v-model.number="filters.minAmount" placeholder="Min Amount" />
      </label>

      <label>
        Max Amount:
        <input type="number" v-model.number="filters.maxAmount" placeholder="Max Amount" />
      </label>

      <button @click="onFilterChange">Search</button>
    </div>

    <!-- Expense List -->
    <ul>
      <li v-for="expense in expenses" :key="expense.id">
        {{ expense.description }} - {{ expense.category }} - {{ expense.amount }} - {{ expense.date }}
      </li>
    </ul>

    <!-- Pagination Controls -->
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">Previous</button>
      <span>Page {{ page }} of {{ totalPages }}</span>
      <button @click="nextPage" :disabled="page >= totalPages">Next</button>
    </div>

    <!-- Total Expenses Count (Optional) -->
    <div class="total-expenses">
      Total Expenses: {{ totalExpenses }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ExpenseList',
  data() {
    return {
      expenses: [],
      // Pagination info
      page: 1,
      size: 10,
      totalPages: 1,
      totalExpenses: 0,
      // Filter criteria
      filters: {
        description: '',
        category: '',
        startDate: '',
        endDate: '',
        minAmount: null,
        maxAmount: null
      }
    };
  },
  methods: {
    // When filter criteria change, reset page number and fetch data
    onFilterChange() {
      this.page = 1;
      this.fetchExpenses();
    },
    // Fetch expenses data using the current filter and pagination parameters
    fetchExpenses() {
      const auth = localStorage.getItem('auth');
      const params = {
        page: this.page,
        size: this.size,
        // Only include non-empty filter parameters
        ...(this.filters.description && { description: this.filters.description }),
        ...(this.filters.category && { category: this.filters.category }),
        ...(this.filters.startDate && { startDate: this.filters.startDate }),
        ...(this.filters.endDate && { endDate: this.filters.endDate }),
        ...(this.filters.minAmount != null && { minAmount: this.filters.minAmount }),
        ...(this.filters.maxAmount != null && { maxAmount: this.filters.maxAmount })
      };

      axios.get('http://localhost:8080/api/expenses', {
        headers: { 'Authorization': `Basic ${auth}` },
        params
      })
          .then(response => {
            // Assume the backend returns a response in the format:
            // { data: [expenses], totalPages: number, totalExpenses: number }
            this.expenses = response.data.data;
            this.totalPages = response.data.totalPages;
            this.totalExpenses = response.data.totalExpenses;
          })
          .catch(error => {
            console.error("Error fetching expenses:", error);
          });
    },
    nextPage() {
      if (this.page < this.totalPages) {
        this.page++;
        this.fetchExpenses();
      }
    },
    prevPage() {
      if (this.page > 1) {
        this.page--;
        this.fetchExpenses();
      }
    }
  },
  mounted() {
    this.fetchExpenses();
  }
};
</script>

<style scoped>
.expense-list {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.filter-controls {
  margin-bottom: 20px;
}

.filter-controls label {
  margin-right: 10px;
  display: inline-block;
  margin-bottom: 5px;
}

.pagination {
  margin-top: 20px;
}

.pagination button {
  margin: 0 5px;
}
</style>
