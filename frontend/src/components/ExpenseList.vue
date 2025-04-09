<template>
  <div>
    <h2>Expenses</h2>
    <!-- ExpenseForm 组件 -->
    <expense-form @expense-added="fetchExpenses" />

    <!-- Filter Controls -->
    <div class="filters">
      <input v-model="filters.category" placeholder="Category" />
      <input type="date" v-model="filters.startDate" placeholder="Start Date" />
      <input type="date" v-model="filters.endDate" placeholder="End Date" />
      <input v-model.number="filters.minAmount" type="number" placeholder="Min Amount" step="0.01" />
      <input v-model.number="filters.maxAmount" type="number" placeholder="Max Amount" step="0.01" />
      <button @click="applyFilters">Apply Filters</button>
    </div>

    <!-- Error Message Display -->
    <div class="error" v-if="errorMessage">
      {{ errorMessage }}
    </div>

    <!-- Expense List -->
    <ul>
      <li v-for="expense in expenses" :key="expense.id">
        {{ expense.description }} - {{ expense.amount.toFixed(2) }} - {{ expense.date }} - {{ expense.category }}
        <!-- Archive button instead of Delete -->
        <button @click="archiveExpense(expense.id)">Archive</button>
      </li>
    </ul>

    <!-- Pagination Controls -->
    <div class="pagination">
      <button @click="previousPage" :disabled="page <= 0">Previous</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button @click="nextPage" :disabled="page >= totalPages - 1">Next</button>
    </div>

    <button @click="logout">Logout</button>

    <!-- Section to view archived expenses -->
    <div class="archived-section">
      <h2>Archived Expenses</h2>
      <button @click="toggleArchived">
        {{ showArchived ? 'Hide Archived' : 'Show Archived' }}
      </button>
      <ul v-if="showArchived">
        <li v-for="expense in archivedExpenses" :key="expense.id">
          {{ expense.description }} - {{ expense.amount.toFixed(2) }} - {{ expense.date }} - {{ expense.category }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import ExpenseForm from './ExpenseForm.vue';

export default {
  components: { ExpenseForm },
  data() {
    return {
      expenses: [],
      archivedExpenses: [],
      page: 0,
      size: 10,
      totalPages: 0,
      errorMessage: '',
      showArchived: false,
      filters: {
        category: '',
        startDate: '',
        endDate: '',
        minAmount: null,
        maxAmount: null,
      }
    };
  },
  mounted() {
    this.fetchExpenses();
  },
  methods: {
    fetchExpenses() {
      const auth = localStorage.getItem('auth');
      if (!auth) {
        this.$router.push('/');
        return;
      }
      // Build request parameters including filtering and pagination options
      const params = {
        page: this.page,
        size: this.size,
        ...(this.filters.category ? { category: this.filters.category } : {}),
        ...(this.filters.startDate ? { startDate: this.filters.startDate } : {}),
        ...(this.filters.endDate ? { endDate: this.filters.endDate } : {}),
        ...(this.filters.minAmount != null ? { minAmount: this.filters.minAmount } : {}),
        ...(this.filters.maxAmount != null ? { maxAmount: this.filters.maxAmount } : {}),
      };
      axios.get('http://localhost:8080/api/expenses', {
        headers: { 'Authorization': `Basic ${auth}` },
        params: params,
      })
          .then(response => {
            // 假设后端返回结构为 { data: [...], totalPages: number, totalExpenses: number }
            this.expenses = response.data.data;
            this.totalPages = response.data.totalPages;
            this.errorMessage = '';
          })
          .catch(error => {
            this.errorMessage = 'Failed to fetch expenses. Please try again later.';
            console.error('Error fetching expenses:', error);
          });
    },
    archiveExpense(id) {
      const auth = localStorage.getItem('auth');
      axios.delete(`http://localhost:8080/api/expenses/${id}`, {
        headers: { 'Authorization': `Basic ${auth}` }
      })
          .then(() => {
            // Refresh the expense list after archiving
            this.fetchExpenses();
          })
          .catch(error => {
            this.errorMessage = 'Failed to archive expense. Please try again later.';
            console.error('Error archiving expense:', error);
          });
    },
    logout() {
      localStorage.removeItem('auth');
      this.$router.push('/');
    },
    applyFilters() {
      // Reset to first page when filters change
      this.page = 0;
      this.fetchExpenses();
    },
    previousPage() {
      if (this.page > 0) {
        this.page--;
        this.fetchExpenses();
      }
    },
    nextPage() {
      if (this.page < this.totalPages - 1) {
        this.page++;
        this.fetchExpenses();
      }
    },
    toggleArchived() {
      this.showArchived = !this.showArchived;
      if (this.showArchived) {
        this.fetchArchivedExpenses();
      }
    },
    fetchArchivedExpenses() {
      const auth = localStorage.getItem('auth');
      axios.get('http://localhost:8080/api/expenses/archived', {
        headers: { 'Authorization': `Basic ${auth}` }
      })
          .then(response => {
            // 假设返回的 JSON 为归档的费用列表
            this.archivedExpenses = response.data;
          })
          .catch(error => {
            this.errorMessage = 'Failed to fetch archived expenses. Please try again later.';
            console.error('Error fetching archived expenses:', error);
          });
    }
  }
};
</script>

<style scoped>
.filters {
  margin-bottom: 1rem;
}
.filters input {
  margin-right: 0.5rem;
  padding: 0.25rem;
}
.pagination {
  margin-top: 1rem;
}
.pagination button {
  margin-right: 0.5rem;
}
.error {
  color: red;
  margin-bottom: 1rem;
}
.archived-section {
  margin-top: 2rem;
  border-top: 1px solid #ccc;
  padding-top: 1rem;
}
</style>
