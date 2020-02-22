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
package com.msindwan.shoebox.views.settings.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.OffsetDateTimeRange
import org.threeten.bp.LocalDate


/**
 * Settings view shared state model.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private var dal: DataAccessLayer = DataAccessLayer.getInstance(application)

    private val budgetScheduleMonth: MutableLiveData<Int> =
        MutableLiveData(LocalDate.now().monthValue)
    private val budgetScheduleYear: MutableLiveData<Int> = MutableLiveData(LocalDate.now().year)
    private val monthlyBudgets: MutableLiveData<List<Budget?>> by lazy {
        MutableLiveData(loadMonthlyBudgets())
    }

    /**
     * Returns the selected year for the budget schedule.
     *
     * @returns <LiveData> The selected budget year.
     */
    fun getBudgetScheduleYear(): LiveData<Int> {
        return budgetScheduleYear
    }

    /**
     * Sets the selected year for the budget schedule.
     *
     * @param year {Int} The year to set.
     */
    fun setBudgetScheduleYear(year: Int) {
        budgetScheduleYear.value = year
        updateMonthlyBudgets()
    }

    /**
     * Returns the selected month for the budget schedule.
     *
     * @returns <LiveData> The selected budget month.
     */
    fun getBudgetScheduleMonth(): LiveData<Int> {
        return budgetScheduleMonth
    }

    /**
     * Sets the selected month for the budget schedule.
     *
     * @param month {Int} The month to set.
     */
    fun setBudgetScheduleMonth(month: Int) {
        budgetScheduleMonth.value = month
    }

    /**
     * Returns the monthly budgets for the selected year.
     *
     * @returns <LiveData> The monthly budgets for the year.
     */
    fun getMonthlyBudgets(): LiveData<List<Budget?>> {
        return monthlyBudgets
    }

    /**
     * Re-fetches monthly budgets.
     */
    private fun updateMonthlyBudgets() {
        monthlyBudgets.value = loadMonthlyBudgets()
    }

    /**
     * Fetches monthly budgets for the selected year.
     *
     * @returns The list of budgets for the year. The list will include one entry for every month in
     * the selected year.
     */
    private fun loadMonthlyBudgets(): List<Budget?> {
        return dal.budgetDAO.getBudgets(OffsetDateTimeRange.year(budgetScheduleYear.value!!))
    }
}
