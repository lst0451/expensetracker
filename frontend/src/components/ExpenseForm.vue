<template>
  <div>
    <h2>Add Expense</h2>
    <form @submit.prevent="addExpense">
      <div>
        <label>Description:</label>
        <input v-model="description" placeholder="Enter description" required />
      </div>
      <div>
        <label>Category:</label>
        <select v-model="category">
          <option value="">Select a category</option>
          <option value="Food">Food</option>
          <option value="Travel">Travel</option>
          <option value="Shopping">Shopping</option>
          <!-- Additional categories -->
        </select>
      </div>
      <div>
        <label>Amount:</label>
        <input v-model.number="amount" type="number" placeholder="Enter amount" required />
      </div>
      <div>
        <label>Date:</label>
        <input v-model="date" type="date" required />
      </div>
      <!-- Display error message if submission fails -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      <button type="submit">Add Expense</button>
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
      date: '',
      error: ''
    };
  },
  methods: {
    addExpense() {
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
            this.$emit('expense-added', response.data);
            this.description = '';
            this.category = '';
            this.amount = null;
            this.date = '';
          })
          .catch(error => {
            // If server returns a validation error, show friendly error message
            if (error.response && error.response.status === 400) {
              // Combine field errors into a single string
              const errors = error.response.data.errors;
              const errorMessages = Object.values(errors).join(' | ');
              this.error = errorMessages || 'Validation error occurred';
            } else {
              this.error = 'An error occurred while adding expense';
            }
            console.error("Error adding expense:", error);
          });
    }
  }
};
</script>

<style scoped>
.error-message {
  color: red;
  margin-bottom: 10px;
}
</style>
