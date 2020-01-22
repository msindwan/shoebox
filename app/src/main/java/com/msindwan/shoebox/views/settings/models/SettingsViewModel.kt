package com.msindwan.shoebox.views.settings.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.dao.BudgetDAO
import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.Currency
import com.msindwan.shoebox.data.entities.Interval
import org.threeten.bp.LocalDate

class SettingsViewModel(application: Application): AndroidViewModel(application) {
    private var dal: DataAccessLayer = DataAccessLayer.getInstance(application)

    private val budgetScheduleYear: MutableLiveData<Int> = MutableLiveData(LocalDate.now().year)
    private val budgetScheduleMonth: MutableLiveData<Int> = MutableLiveData(LocalDate.now().monthValue)
    private val monthlyBudgets: MutableLiveData<Array<Budget?>> by lazy {
        MutableLiveData(loadMonthlyBudgets())
    }

    fun getBudgetScheduleYear(): LiveData<Int> {
        return budgetScheduleYear
    }

    fun setBudgetScheduleYear(year: Int) {
        budgetScheduleYear.value = year
        updateMonthlyBudgets()
    }

    fun getBudgetScheduleMonth(): LiveData<Int> {
        return budgetScheduleMonth
    }

    fun setBudgetScheduleMonth(month: Int) {
        budgetScheduleMonth.value = month
    }

    fun getMonthlyBudgets(): LiveData<Array<Budget?>> {
        return monthlyBudgets
    }

    fun insertOrUpdateBudget(month: Int, year: Int, amount: Long, interval: Interval, currency: Currency = Currency.USD) {
        dal.budgetDAO.upsertBudget(month, year, interval, amount, currency)
    }

    private fun updateMonthlyBudgets() {
        monthlyBudgets.value = loadMonthlyBudgets()
    }

    private fun loadMonthlyBudgets(): Array<Budget?> {
        val budgets = dal.budgetDAO.getBudgetsForYear(budgetScheduleYear.value!!, BudgetDAO.Companion.Order.DATE_DESC)
        val monthBudgets: Array<Budget?> = Array(12) { null }

        for (i in 1..12) {
            for (budget in budgets) {
                if (
                    (budget.month == i && budget.year == budgetScheduleYear.value!!) ||
                    (budget.month == i && budget.interval == Interval.Y) ||
                    ((i > budget.month || budgetScheduleYear.value!! > budget.year) && budget.interval == Interval.M)
                ) {
                    monthBudgets[i - 1] = budget.copy()
                    break
                }
            }
        }

        return monthBudgets
    }
}
