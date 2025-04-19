<template>
  <div class="expense-form-container">
    <h2 class="form-title">Add Expense</h2>
    <form @submit.prevent="addExpense" class="expense-form">
      <div class="form-group">
        <label for="description">Description:</label>
        <input
            id="description"
            v-model="description"
            placeholder="Enter expense description"
            required
            class="form-control"
        />
      </div>

      <div class="form-group">
        <label for="category">Category:</label>
        <select
            id="category"
            v-model="category"
            required
            class="form-control"
        >
          <option value="" disabled>Select a category</option>
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

      <div class="form-group">
        <label for="amount">Amount:</label>
        <div class="amount-input-container">
          <span class="currency-symbol">$</span>
          <input
              id="amount"
              v-model.number="amount"
              type="number"
              placeholder="Enter amount"
              required
              class="form-control amount-input"
              min="0"
              step="0.01"
          />
        </div>
      </div>

      <div class="form-group">
        <label for="date">Date:</label>
        <input
            id="date"
            v-model="date"
            type="date"
            required
            class="form-control"
        />
      </div>

      <transition name="fade">
        <div v-if="error" class="error-message">
          <i class="error-icon">⚠️</i> {{ error }}
        </div>
      </transition>
      
      <!-- Field-specific validation errors -->
      <transition name="fade">
        <div v-if="validationErrors.amount" class="validation-error">
          <i class="error-icon">⚠️</i> {{ validationErrors.amount }}
        </div>
      </transition>
      
      <transition name="fade">
        <div v-if="validationErrors.date" class="validation-error">
          <i class="error-icon">⚠️</i> {{ validationErrors.date }}
        </div>
      </transition>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="resetForm">Reset</button>
        <button type="submit" class="btn btn-primary">
          <span v-if="isSubmitting" class="spinner"></span>
          <span v-else>Add Expense</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ExpenseForm',
  data() {
    return {
      description: '',
      category: '',
      amount: null,
      date: new Date().toISOString().substr(0, 10), // Default to today
      error: '',
      validationErrors: {
        amount: '',
        date: ''
      },
      isSubmitting: false
    };
  },
  methods: {
    addExpense() {
      // Reset validation errors
      this.error = '';
      this.validationErrors = {
        amount: '',
        date: ''
      };
      
      // Client-side validation
      let isValid = true;
      
      if (!this.category) {
        this.error = 'Please select a category';
        isValid = false;
      }
      
      if (this.amount !== null && this.amount <= 0) {
        this.validationErrors.amount = 'Amount must be greater than 0';
        isValid = false;
      }
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const selectedDate = new Date(this.date);
      selectedDate.setHours(0, 0, 0, 0);
      
      if (selectedDate > today) {
        this.validationErrors.date = 'Date must not be in the future';
        isValid = false;
      }
      
      if (!isValid) {
        return;
      }

      this.isSubmitting = true;
      const auth = localStorage.getItem('auth');

      axios.post('http://localhost:8080/api/expenses', {
        description: this.description,
        category: this.category,
        amount: this.amount,
        date: this.date
      }, {
        headers: { 'Authorization': `Basic ${auth}` }
      })
          .then(response => {
            // Clear form and error message on successful submission
            this.error = '';
            this.validationErrors = { amount: '', date: '' };
            this.$emit('expense-added', response.data);
            this.showSuccessMessage();
            this.resetForm();
          })
          .catch(error => {
            // If server returns validation error, show friendly error message
            if (error.response && error.response.status === 400) {
              // Handle field-specific errors
              const errors = error.response.data.errors;
              
              if (typeof errors === 'object') {
                // Clear previous errors
                this.validationErrors = { amount: '', date: '' };
                
                // Set field-specific errors
                if (errors.amount) {
                  this.validationErrors.amount = errors.amount;
                }
                
                if (errors.date) {
                  this.validationErrors.date = errors.date;
                }
                
                // Set general error if there are other fields with errors
                const otherErrors = Object.entries(errors)
                  .filter(([key]) => key !== 'amount' && key !== 'date')
                  .map(([_, value]) => value);
                
                if (otherErrors.length > 0) {
                  this.error = otherErrors.join(' | ');
                }
              } else {
                // If errors is not an object, set general error
                this.error = 'Validation error: ' + (error.response.data.message || 'Please check your input');
              }
            } else {
              this.error = 'An error occurred while adding expense';
            }
            console.error("Error adding expense:", error);
          })
          .finally(() => {
            this.isSubmitting = false;
          });
    },

    resetForm() {
      this.description = '';
      this.category = '';
      this.amount = null;
      this.date = new Date().toISOString().substr(0, 10);
      this.error = '';
      this.validationErrors = {
        amount: '',
        date: ''
      };
    },

    showSuccessMessage() {
      // Implement a success message notification
      this.$emit('show-toast', {
        type: 'success',
        message: 'Expense added successfully!'
      });
    }
  }
};
</script>

<style scoped>
.expense-form-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.form-title {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 24px;
  font-weight: 600;
  border-bottom: 2px solid #3498db;
  padding-bottom: 10px;
}

.expense-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 5px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
}

.form-control {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.2s;
  font-size: 14px;
  width: 100%;
}

.form-control:focus {
  border-color: #3498db;
  outline: none;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.form-control::placeholder {
  color: #c0c4cc;
}

.amount-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.currency-symbol {
  position: absolute;
  left: 12px;
  color: #606266;
}

.amount-input {
  padding-left: 24px;
}

.error-message, .validation-error {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.validation-error {
  background-color: #fff6f6;
  border-left: 3px solid #f56c6c;
}

.error-icon {
  margin-right: 8px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-secondary {
  background-color: #f8f9fa;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.btn-secondary:hover {
  background-color: #e9ecef;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 0.8s ease infinite;
  margin-right: 6px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

@media (max-width: 576px) {
  .expense-form-container {
    padding: 15px;
    box-shadow: none;
  }

  .form-group label {
    font-size: 13px;
  }

  .form-control {
    padding: 8px 10px;
  }

  .btn {
    padding: 8px 16px;
  }
}
</style>
