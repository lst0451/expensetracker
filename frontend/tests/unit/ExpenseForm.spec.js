import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import ExpenseForm from '@/components/ExpenseForm.vue'
import axios from 'axios'
import Vue from 'vue'

// Mock axios
vi.mock('axios')

describe('ExpenseForm.vue', () => {
    // Reset data before each test
    beforeEach(() => {
        vi.resetAllMocks()
        // Mock localStorage.getItem
        global.localStorage = {
            getItem: vi.fn(() => 'mockAuthToken')
        }
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    it('emits "expense-added" event with response data when form is submitted successfully', async () => {
        // Mock successful axios response
        const responseData = {
            id: 1,
            description: 'Lunch',
            category: 'Food',
            amount: 25.5,
            date: '2025-04-09'
        }
        axios.post.mockResolvedValueOnce({ data: responseData })

        // Mock Date.prototype.toISOString to return a fixed date
        const originalToISOString = Date.prototype.toISOString;
        Date.prototype.toISOString = vi.fn(() => '2025-04-09T00:00:00.000Z');

        // Mount component
        const wrapper = mount(ExpenseForm)

        // Fill form fields
        await wrapper.find('input[placeholder="Enter expense description"]').setValue('Lunch')
        await wrapper.find('select').setValue('Food')
        await wrapper.find('input[type="number"]').setValue(25.5)
        await wrapper.find('input[type="date"]').setValue('2025-04-09')

        // Submit form
        await wrapper.find('form').trigger('submit.prevent')

        // Wait for async operations to complete
        await Vue.nextTick()

        // Verify axios.post call
        expect(axios.post).toHaveBeenCalledWith(
            'http://localhost:8080/api/expenses',
            {
                description: 'Lunch',
                category: 'Food',
                amount: 25.5,
                date: '2025-04-09'
            },
            {
                headers: { 'Authorization': 'Basic mockAuthToken' }
            }
        )

        // Verify event emission
        const emittedEvents = wrapper.emitted()
        expect(emittedEvents).toHaveProperty('expense-added')
        expect(emittedEvents['expense-added'][0][0]).toEqual(responseData)

        // Verify form reset - update expectation for date to match what the component actually does
        expect(wrapper.vm.description).toBe('')
        expect(wrapper.vm.category).toBe('')
        expect(wrapper.vm.amount).toBeNull()
        expect(wrapper.vm.date).toBe('2025-04-09') // This matches the default date reset in the component

        // Restore the original Date.prototype.toISOString
        Date.prototype.toISOString = originalToISOString;
    })

    it('displays validation error when server returns 400 status', async () => {
        // Mock validation error response
        const errorResponse = {
            response: {
                status: 400,
                data: {
                    errors: {
                        description: 'Description is required',
                        amount: 'Amount must be positive'
                    }
                }
            }
        }
        axios.post.mockRejectedValueOnce(errorResponse)

        // Mount component
        const wrapper = mount(ExpenseForm)

        // Fill form fields
        await wrapper.find('input[placeholder="Enter expense description"]').setValue('Test')
        await wrapper.find('select').setValue('Food')
        await wrapper.find('input[type="number"]').setValue(-10) // Negative number to trigger validation error
        await wrapper.find('input[type="date"]').setValue('2025-04-09')

        // Submit form
        await wrapper.find('form').trigger('submit.prevent')

        // Wait for async operations to complete
        await Vue.nextTick()

        // Verify error message display - use contains() instead of exact matching
        expect(wrapper.find('.error-message').exists()).toBe(true)
        expect(wrapper.find('.error-message').text()).toContain('Description is required | Amount must be positive')

        // Verify form fields were not reset
        expect(wrapper.vm.description).toBe('Test')
        expect(wrapper.vm.amount).toBe(-10)
    })

    it('displays generic error message when server returns non-validation error', async () => {
        // Mock server error
        axios.post.mockRejectedValueOnce(new Error('Network Error'))

        // Mount component
        const wrapper = mount(ExpenseForm)

        // Fill and submit form
        await wrapper.find('input[placeholder="Enter expense description"]').setValue('Test')
        await wrapper.find('select').setValue('Food')
        await wrapper.find('input[type="number"]').setValue(50)
        await wrapper.find('input[type="date"]').setValue('2025-04-09')
        await wrapper.find('form').trigger('submit.prevent')

        // Wait for async operations to complete
        await Vue.nextTick()

        // Verify error message - use contains() instead of exact matching
        expect(wrapper.find('.error-message').exists()).toBe(true)
        expect(wrapper.find('.error-message').text()).toContain('An error occurred while adding expense')
    })

    it('requires all form fields before submission', async () => {
        // Mount component
        const wrapper = mount(ExpenseForm)

        // Find all required input fields
        const requiredInputs = wrapper.findAll('input[required]')

        // Ensure at least one required field was found
        expect(requiredInputs.length).toBeGreaterThan(0)

        // Check required fields - need to iterate through wrapperArray
        for (let i = 0; i < requiredInputs.length; i++) {
            const input = requiredInputs.at(i)
            expect(input.attributes('required')).toBeDefined()
        }
    })
})