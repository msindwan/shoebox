package com.msindwan.shoebox.views.dashboard.models

import android.app.Application
import androidx.lifecycle.*
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.entities.*
import com.msindwan.shoebox.views.dashboard.components.FooterMenu
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate


class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private var dal: DataAccessLayer = DataAccessLayer.getInstance(application)

    private val recentTransactions: MutableLiveData<List<Transaction>> by lazy {
        MutableLiveData(loadRecentTransactions())
    }

    private val searchTransactions: MutableLiveData<List<Transaction>> by lazy {
        MutableLiveData(loadSearchTransactions())
    }

    private val sumOfTransactions: MutableLiveData<Long> by lazy {
        MutableLiveData(loadSumOfTransactions())
    }

    private val budget: MutableLiveData<Budget?> by lazy {
        MutableLiveData(loadBudget())
    }

    private val currentMenuItem: MutableLiveData<FooterMenu.MenuItem> =
        MutableLiveData(FooterMenu.MenuItem.HOME)

    private var selectedDateRange: LocalDateRange = LocalDateRange.currentMonth()
    private var searchLastCreatedDate: Instant? = null
    private var searchOrder:  TransactionDAO.Companion.Order = TransactionDAO.Companion.Order.DATE_DESC

    private var searchFilters: SearchFilters =
        SearchFilters(null, LocalDateRange(null, null), null, null, null)

    fun getRecentTransactions(): LiveData<List<Transaction>> {
        return recentTransactions
    }

    fun getSumOfTransactions(): LiveData<Long> {
        return sumOfTransactions
    }

    fun getBudget(): LiveData<Budget?> {
        return budget
    }

    fun getSearchTransactions(): LiveData<List<Transaction>> {
        return searchTransactions
    }

    fun getCurrentMenuItem(): LiveData<FooterMenu.MenuItem> {
        return currentMenuItem
    }

    fun insertTransaction(date: LocalDate, title: String, category: String, amount: Long) {
        dal.transactionDAO.insertTransaction(date, title, category, amount, Currency.USD)
        updateRecentTransactions()
        updateSearchTransactions()
        updateSumOfTransactions()
    }

    fun deleteTransactions(transactions: List<Transaction>) {
        dal.transactionDAO.deleteTransactions(transactions)
        updateRecentTransactions()
        // @todo find a way to retain pagination
        updateSearchTransactions()
        updateSumOfTransactions()
    }

    fun nextSearchTransactions(limit: Int): Int {
        val allTransactions = mutableListOf<Transaction>()
        val transactions: List<Transaction>
        var numTransactions = 0

        var date = searchFilters.dateRange

        if (searchTransactions.value?.size ?: -1 > 0) {
            numTransactions = searchTransactions.value!!.size
            date = LocalDateRange(
                searchTransactions.value!!.last().date,
                searchFilters.dateRange.endDate
            )
            searchLastCreatedDate = searchTransactions.value!!.last().date_created
            allTransactions.addAll(searchTransactions.value!!)
        }

        transactions = dal.transactionDAO.getTransactions(
            SearchFilters(
                searchFilters.title,
                date,
                searchFilters.minAmount,
                searchFilters.maxAmount,
                searchFilters.category
            ),
            searchLastCreatedDate,
            searchOrder,
            limit
        )

        allTransactions.addAll(transactions)
        searchTransactions.value = allTransactions.distinct()
        return allTransactions.size - numTransactions
    }

    fun setCurrentMenuItem(menuItem: FooterMenu.MenuItem) {
        currentMenuItem.value = menuItem
    }

    fun setSearchTransactionsFilters(filters: SearchFilters) {
        if (filters != searchFilters) {
            searchFilters = filters
            updateSearchTransactions()
        }
    }

    fun getSearchTransactionsFilters(): SearchFilters {
        return searchFilters.copy()
    }

    private fun loadRecentTransactions(): List<Transaction> {
        val filters = SearchFilters(null, selectedDateRange, null, null, null)
        return dal.transactionDAO.getTransactions(
            filters,
            null,
            TransactionDAO.Companion.Order.DATE_DESC,
            4
        )
    }

    fun updateBudget() {
        budget.value = loadBudget()
    }

    private fun updateRecentTransactions() {
        recentTransactions.value = loadRecentTransactions()
    }

    private fun loadSearchTransactions(): List<Transaction> {
        return dal.transactionDAO.getTransactions(
            searchFilters,
            null,
            searchOrder,
            100
        )
    }

    private fun updateSearchTransactions() {
        searchTransactions.value = loadSearchTransactions()
    }

    private fun updateSumOfTransactions() {
        sumOfTransactions.value = loadSumOfTransactions()
    }

    private fun loadBudget(): Budget? {
        // @todo: don't hard-code this
        return dal.budgetDAO.getBudgetForMonth(
            1,
            2020
        )
    }

    private fun loadSumOfTransactions(): Long {
        return dal.transactionDAO.getSumOfTransactions(selectedDateRange)
    }
}
