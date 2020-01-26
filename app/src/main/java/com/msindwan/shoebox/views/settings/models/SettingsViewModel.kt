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
import com.msindwan.shoebox.data.entities.LocalDateRange
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
        return dal.budgetDAO.getBudgets(
            LocalDateRange(
                LocalDate.of(budgetScheduleYear.value!!, 1, 1),
                LocalDate.of(budgetScheduleYear.value!!, 12, 31)
            )
        )
    }
}
