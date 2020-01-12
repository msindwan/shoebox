package com.msindwan.shoebox.data.dao

import com.msindwan.shoebox.data.entities.Budget

interface BudgetDAO {
    fun getBudget(id: Int): Budget?
    fun getBudgets(startDate: Long, endDate: Long): MutableList<Budget>
    fun upsertBudget(startDate: Long, endDate: Long, amount: Long, currency: String, interval: String)
    fun deleteBudget(budget: Budget)
}
