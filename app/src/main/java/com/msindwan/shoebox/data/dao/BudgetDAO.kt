/**
 * Copyright (C) 2020 Mayank Sindwani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msindwan.shoebox.data.dao

import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.Currency
import com.msindwan.shoebox.data.entities.Interval


/**
 * Budget Data Access Object.
 */
interface BudgetDAO {
    companion object {
        enum class Order(val value: String) {
            DATE_ASC("ASC"),
            DATE_DESC("DESC")
        }
    }

    /**
     * Returns the budget for the given month.
     *
     * @param month {Int} Integer from 1 - 12 representing the month.
     * @param year {Int} The year.
     * @returns The budget for the month if found.
     */
    fun getBudgetForMonth(month: Int, year: Int): Budget?

    /**
     * Returns budgets applicable for the given year.
     *
     * @param year {Int} The year.
     * @returns The budgets that fall within the year provided.
     */
    fun getBudgetsForYear(year: Int, order: Order = Order.DATE_ASC): List<Budget>

    /**
     * Updates or inserts a budget.
     *
     * @param month {Int} The month that the budget applies to.
     * @param year {Int} The year that the budget applies to.
     * @param amount (Long} The budget amount in cents.
     * @param currency {Currency} The currency to store the amount in.
     * @param interval {Interval} The interval that the budget repeats for.
     */
    fun upsertBudget(month: Int, year: Int, interval: Interval, amount: Long, currency: Currency)
}
