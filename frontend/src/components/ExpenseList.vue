<template>
  <div class="expenses-container">
    <div class="header-section">
      <h1 class="page-title">Expenses Dashboard</h1>
      <button @click="logout" class="logout-btn">
        <span class="logout-icon">↪</span> Logout
      </button>
    </div>

    <!-- ExpenseForm Component -->
    <div class="form-section">
      <expense-form @expense-added="fetchExpenses" @show-toast="handleToast" />
    </div>

    <!-- Toast Notification -->
    <transition name="toast-fade">
      <div v-if="toast.show" class="toast" :class="'toast-' + toast.type">
        {{ toast.message }}
      </div>
    </transition>

    <!-- Main Content Tabs -->
    <div class="tabs-container">
      <div class="tabs">
        <button
            @click="activeTab = 'current'"
            :class="{ 'active': activeTab === 'current' }"
            class="tab-btn"
        >
          Current Expenses
        </button>
        <button
            @click="activeTab = 'archived'; fetchArchivedIfNeeded()"
            :class="{ 'active': activeTab === 'archived' }"
            class="tab-btn"
        >
          Archived Expenses
        </button>
      </div>

      <!-- Current Expenses Tab Content -->
      <div v-if="activeTab === 'current'" class="tab-content">
        <!-- Filter Controls -->
        <div class="filter-section">
          <h3 class="section-title">Filters</h3>
          <div class="filters-grid">
            <div class="filter-group">
              <label for="category-filter">Category</label>
              <select
                  id="category-filter"
                  v-model="filters.category"
                  class="filter-input"
              >
                <option value="">All Categories</option>
                <option value="Food">Food</option>
                <option value="Travel">Travel</option>
                <option value="Shopping">Shopping</option>
                <option value="Entertainment">Entertainment</option>
                <option value="Healthcare">Healthcare</option>
                <option value="Education">Education</option>
                <option value="Utilities">Utilities</option>
                <option value="Others">Others</option>
              </select>
            </div>

            <div class="filter-group">
              <label for="startDate-filter">Start Date</label>
              <input
                  id="startDate-filter"
                  type="date"
                  v-model="filters.startDate"
                  class="filter-input"
              />
            </div>

            <div class="filter-group">
              <label for="endDate-filter">End Date</label>
              <input
                  id="endDate-filter"
                  type="date"
                  v-model="filters.endDate"
                  class="filter-input"
              />
            </div>

            <div class="filter-group">
              <label for="minAmount-filter">Min Amount</label>
              <input
                  id="minAmount-filter"
                  v-model.number="filters.minAmount"
                  type="number"
                  placeholder="0.00"
                  step="0.01"
                  class="filter-input"
              />
            </div>

            <div class="filter-group">
              <label for="maxAmount-filter">Max Amount</label>
              <input
                  id="maxAmount-filter"
                  v-model.number="filters.maxAmount"
                  type="number"
                  placeholder="Max value"
                  step="0.01"
                  class="filter-input"
              />
            </div>

            <div class="filter-actions">
              <button @click="applyFilters" class="apply-btn">Apply Filters</button>
              <button @click="resetFilters" class="reset-btn">Reset</button>
            </div>
          </div>
        </div>

        <!-- Error Message Display -->
        <div v-if="errorMessage" class="error-message">
          <span class="error-icon">⚠️</span> {{ errorMessage }}
        </div>

        <!-- Expense List -->
        <div class="expenses-section">
          <h3 class="section-title">Expense List</h3>
          <div v-if="isLoading" class="loading-spinner">Loading...</div>

          <div v-else-if="expenses.length === 0" class="empty-state">
            <p>No expenses found. Add a new expense using the form above.</p>
          </div>

          <div v-else class="expense-list">
            <div class="expense-list-header">
              <span class="col description-col">Description</span>
              <span class="col category-col">Category</span>
              <span class="col date-col">Date</span>
              <span class="col amount-col">Amount</span>
              <span class="col action-col">Action</span>
            </div>

            <div
                v-for="expense in expenses"
                :key="expense.id"
                class="expense-item"
            >
              <span class="col description-col">{{ expense.description }}</span>
              <span class="col category-col">
                <span class="category-badge">{{ expense.category }}</span>
              </span>
              <span class="col date-col">{{ formatDate(expense.date) }}</span>
              <span class="col amount-col">${{ expense.amount.toFixed(2) }}</span>
              <span class="col action-col">
                <button @click="archiveExpense(expense.id)" class="archive-btn">
                  Archive
                </button>
              </span>
            </div>
          </div>

          <!-- Pagination Controls -->
          <div v-if="expenses.length > 0" class="pagination">
            <button
                @click="previousPage"
                :disabled="page <= 0"
                class="page-btn"
            >
              &laquo; Previous
            </button>
            <span class="page-info">Page {{ page + 1 }} of {{ totalPages || 1 }}</span>
            <button
                @click="nextPage"
                :disabled="page >= totalPages - 1"
                class="page-btn"
            >
              Next &raquo;
            </button>
          </div>
        </div>
      </div>

      <!-- Archived Expenses Tab Content -->
      <div v-if="activeTab === 'archived'" class="tab-content">
        <h3 class="section-title">Archived Expenses</h3>

        <div v-if="isLoadingArchived" class="loading-spinner">Loading archived expenses...</div>

        <div v-else-if="archivedExpenses.length === 0" class="empty-state">
          <p>No archived expenses found.</p>
        </div>

        <div v-else class="expense-list">
          <div class="expense-list-header">
            <span class="col description-col">Description</span>
            <span class="col category-col">Category</span>
            <span class="col date-col">Date</span>
            <span class="col amount-col">Amount</span>
          </div>

          <div
              v-for="expense in archivedExpenses"
              :key="expense.id"
              class="expense-item archived"
          >
            <span class="col description-col">{{ expense.description }}</span>
            <span class="col category-col">
              <span class="category-badge">{{ expense.category }}</span>
            </span>
            <span class="col date-col">{{ formatDate(expense.date) }}</span>
            <span class="col amount-col">${{ expense.amount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
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
      isLoading: false,
      isLoadingArchived: false,
      activeTab: 'current',
      archivedFetched: false,
      toast: {
        show: false,
        message: '',
        type: 'info'
      },
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

      this.isLoading = true;
      this.errorMessage = '';

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
            // Assuming backend returns structure { data: [...], totalPages: number, totalExpenses: number }
            this.expenses = response.data.data;
            this.totalPages = response.data.totalPages;
            this.errorMessage = '';
          })
          .catch(error => {
            this.errorMessage = 'Failed to fetch expenses. Please try again later.';
            console.error('Error fetching expenses:', error);
          })
          .finally(() => {
            this.isLoading = false;
          });
    },

    archiveExpense(id) {
      const auth = localStorage.getItem('auth');

      this.isLoading = true;

      axios.delete(`http://localhost:8080/api/expenses/${id}`, {
        headers: { 'Authorization': `Basic ${auth}` }
      })
          .then(() => {
            // Refresh the expense list after archiving
            this.fetchExpenses();
            this.showToast('Expense archived successfully', 'success');
          })
          .catch(error => {
            this.errorMessage = 'Failed to archive expense. Please try again later.';
            console.error('Error archiving expense:', error);
          })
          .finally(() => {
            this.isLoading = false;
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

    resetFilters() {
      this.filters = {
        category: '',
        startDate: '',
        endDate: '',
        minAmount: null,
        maxAmount: null,
      };
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

    fetchArchivedIfNeeded() {
      if (!this.archivedFetched) {
        this.fetchArchivedExpenses();
      }
    },

    fetchArchivedExpenses() {
      const auth = localStorage.getItem('auth');

      this.isLoadingArchived = true;
      this.errorMessage = '';

      axios.get('http://localhost:8080/api/expenses/archived', {
        headers: { 'Authorization': `Basic ${auth}` }
      })
          .then(response => {
            // Assuming the JSON returned is the list of archived expenses
            this.archivedExpenses = response.data;
            this.archivedFetched = true;
          })
          .catch(error => {
            this.errorMessage = 'Failed to fetch archived expenses. Please try again later.';
            console.error('Error fetching archived expenses:', error);
          })
          .finally(() => {
            this.isLoadingArchived = false;
          });
    },

    formatDate(dateString) {
      const options = { year: 'numeric', month: 'short', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    },

    showToast(message, type = 'info') {
      this.toast = {
        show: true,
        message,
        type
      };

      // Hide toast after 3 seconds
      setTimeout(() => {
        this.toast.show = false;
      }, 3000);
    },

    handleToast(toastData) {
      this.showToast(toastData.message, toastData.type);
    }
  }
};
</script>

<style scoped>
.expenses-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  color: #2c3e50;
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.logout-btn {
  background-color: #f8f9fa;
  color: #495057;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: #e9ecef;
}

.logout-icon {
  margin-right: 6px;
}

.form-section {
  margin-bottom: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

/* Tabs */
.tabs-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e9ecef;
  background-color: #f8f9fa;
}

.tab-btn {
  padding: 15px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #495057;
  font-weight: 500;
  transition: all 0.2s;
  position: relative;
  flex: 1;
  text-align: center;
}

.tab-btn:hover {
  background-color: #e9ecef;
}

.tab-btn.active {
  color: #3498db;
  background-color: #fff;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #3498db;
}

.tab-content {
  padding: 20px;
}

/* Filter Section */
.filter-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
}

.section-title {
  font-size: 18px;
  margin-bottom: 15px;
  color: #2c3e50;
  font-weight: 600;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  font-size: 14px;
  margin-bottom: 5px;
  color: #495057;
}

.filter-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.apply-btn, .reset-btn {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  border: none;
  transition: all 0.2s;
}

.apply-btn {
  background-color: #3498db;
  color: white;
}

.apply-btn:hover {
  background-color: #2980b9;
}

.reset-btn {
  background-color: #f8f9fa;
  color: #495057;
  border: 1px solid #ddd;
}

.reset-btn:hover {
  background-color: #e9ecef;
}

/* Expense List */
.expenses-section {
  margin-top: 20px;
}

.expense-list {
  border: 1px solid #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.expense-list-header {
  display: flex;
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  padding: 12px 15px;
  border-bottom: 2px solid #e9ecef;
}

.expense-item {
  display: flex;
  padding: 12px 15px;
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.2s;
}

.expense-item:last-child {
  border-bottom: none;
}

.expense-item:hover {
  background-color: #f8f9fa;
}

.expense-item.archived {
  background-color: #f8f9fa;
  color: #6c757d;
}

.col {
  padding: 0 10px;
  display: flex;
  align-items: center;
}

.description-col {
  flex: 2;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-col, .date-col {
  flex: 1;
}

.amount-col {
  flex: 1;
  font-weight: 500;
}

.action-col {
  flex: 1;
  justify-content: flex-end;
}

.category-badge {
  background-color: #e9ecef;
  color: #495057;
  padding: 4px 8px;
  border-radius: 16px;
  font-size: 12px;
}

.archive-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.archive-btn:hover {
  background-color: #5a6268;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  gap: 15px;
}

.page-btn {
  padding: 8px 16px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background-color: #e9ecef;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #6c757d;
  font-size: 14px;
}

/* Toast Notification */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 4px;
  color: white;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-size: 14px;
}

.toast-success {
  background-color: #28a745;
}

.toast-error {
  background-color: #dc3545;
}

.toast-info {
  background-color: #17a2b8;
}

.toast-fade-enter-active, .toast-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.toast-fade-enter, .toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Loading and Error States */
.loading-spinner {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

.error-message {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 12px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.error-icon {
  margin-right: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
  background-color: #f8f9fa;
  border-radius: 4px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .filters-grid {
    grid-template-columns: 1fr;
  }

  .expense-list-header {
    display: none;
  }

  .expense-item {
    flex-direction: column;
    padding: 15px;
    gap: 8px;
  }

  .col {
    padding: 0;
  }

  .description-col {
    font-weight: 600;
    font-size: 16px;
  }

  .category-col, .date-col, .amount-col {
    display: flex;
    width: 100%;
  }

  .action-col {
    margin-top: 8px;
    justify-content: flex-start;
  }

  .category-col::before {
    content: 'Category: ';
    margin-right: 5px;
    font-weight: 500;
  }

  .date-col::before {
    content: 'Date: ';
    margin-right: 5px;
    font-weight: 500;
  }

  .amount-col::before {
    content: 'Amount: ';
    margin-right: 5px;
    font-weight: 500;
  }
}
</style>