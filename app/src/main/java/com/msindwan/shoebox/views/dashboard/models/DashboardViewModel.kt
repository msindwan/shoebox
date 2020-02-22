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
package com.msindwan.shoebox.views.dashboard.models

import android.app.Application
import androidx.lifecycle.*
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.dao.BudgetDAO
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.entities.*
import com.msindwan.shoebox.views.dashboard.components.FooterMenu
import com.msindwan.shoebox.views.dashboard.components.BudgetGraph
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit


/**
 * Dashboard view shared state model.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private var dal: DataAccessLayer = DataAccessLayer.getInstance(application)

    private val recentTransactions: MutableLiveData<List<Transaction>> by lazy {
        MutableLiveData(loadRecentTransactions())
    }

    private val searchTransactions: MutableLiveData<List<Transaction>> by lazy {
        MutableLiveData(loadSearchTransactions())
    }

    private val sumOfRecentTransactions: MutableLiveData<Long> by lazy {
        MutableLiveData(loadSumOfTransactions())
    }

    private val currentBudget: MutableLiveData<Budget?> by lazy {
        MutableLiveData(loadBudget())
    }

    private val currentMenuItem: MutableLiveData<FooterMenu.MenuItem> =
        MutableLiveData(FooterMenu.MenuItem.HOME)

    private val budgetGraph: MutableLiveData<List<BudgetGraph.Point>> by lazy {
        MutableLiveData(loadBudgetGraph())
    }

    private var trendsDateRange: MutableLiveData<OffsetDateTimeRange> =
        MutableLiveData(OffsetDateTimeRange.currentYear().minusEnd(4, ChronoUnit.MONTHS))

    private var recentTransactionsDateRange: OffsetDateTimeRange = OffsetDateTimeRange.currentMonth()

    private var searchOrder: TransactionDAO.Companion.Order =
        TransactionDAO.Companion.Order.DATE_DESC
    private var searchFilters: SearchFilters =
        SearchFilters(null, OffsetDateTimeRange(null, null), null, null, null)

    /**
     * Returns a list of recent transactions.
     *
     * @returns <LiveData> The list of recent transactions.
     */
    fun getRecentTransactions(): LiveData<List<Transaction>> {
        return recentTransactions
    }

    /**
     * Returns the sum of recent transaction.
     *
     * @returns <LiveData> The sum of recent transaction amounts.
     */
    fun getSumOfRecentTransactions(): LiveData<Long> {
        return sumOfRecentTransactions
    }

    /**
     * Returns the budget for the current date range.
     *
     * @returns <LiveData> The budget for the current date range.
     */
    fun getCurrentBudget(): LiveData<Budget?> {
        return currentBudget
    }

    /**
     * Returns transactions for the given search criteria.
     *
     * @returns <LiveData> Transactions for the current search criteria.
     */
    fun getSearchTransactions(): LiveData<List<Transaction>> {
        return searchTransactions
    }

    /**
     * Returns the menu item currently selected.
     *
     * @returns <LiveData> The currently selected menu item.
     */
    fun getCurrentMenuItem(): LiveData<FooterMenu.MenuItem> {
        return currentMenuItem
    }

    /**
     * Inserts a transaction and updates relevant subscribers.
     *
     * @param date {OffsetDateTime} The date of the transaction.
     * @param zoneId {ZoneId} The timezone of the transaction.
     * @param title {String} The title of the transaction.
     * @param category {String} The category of the transaction.
     * @param amount {Long} The amount of the transaction.
     */
    fun insertTransaction(date: OffsetDateTime, zoneId: ZoneId, title: String, category: String, amount: Long) {
        dal.transactionDAO.insertTransaction(date, zoneId, title, category, amount, Currency.USD)
        updateRecentTransactions()
        updateSearchTransactions()
        updateSumOfTransactions()
        updateTrends()
    }

    /**
     * Deletes a transaction and updates relevant subscribers.
     *
     * @param transactions {List<Transaction>} The list of transactions to delete.
     */
    fun deleteTransactions(transactions: List<Transaction>) {
        // @todo find a way to retain pagination
        dal.transactionDAO.deleteTransactions(transactions)
        updateRecentTransactions()
        updateSearchTransactions()
        updateSumOfTransactions()
        updateTrends()
    }

    /**
     * Inserts or updates a budget and updates relevant subscribers.
     *
     * @param month {Int} The month of the budget.
     * @param year {Int} The year of the budget.
     * @param amount {Long} The amount of the budget.
     * @param interval {Interval} The interval of the budget.
     * @param currency {Currency} The currency of the budget amount.
     */
    fun insertOrUpdateBudget(
        month: Int,
        year: Int,
        amount: Long,
        interval: Interval,
        currency: Currency = Currency.USD
    ) {
        dal.budgetDAO.upsertBudget(month, year, interval, amount, currency)
        updateTrends()
        updateBudget()
    }

    /**
     * Paginates across search transactions and updates relevant subscribers.
     *
     * @param limit {Int} The maximum number of results to return in a single search.
     * @returns {Int} The number of transactions found from the latest cursor.
     */
    fun nextSearchTransactions(limit: Int): Int {
        val allTransactions = mutableListOf<Transaction>()
        val transactions: List<Transaction>
        var date = searchFilters.dateRange
        var numTransactions = 0

        if (searchTransactions.value?.isNotEmpty() == true) {
            // Continue from the last transaction date up until the specified end date.
            numTransactions = searchTransactions.value!!.size
            date = OffsetDateTimeRange(
                searchTransactions.value!!.last().date,
                searchFilters.dateRange.endDate
            )
            allTransactions.addAll(searchTransactions.value!!)
        }

        // Fetch new transactions and update the list.
        transactions = dal.transactionDAO.getTransactions(
            SearchFilters(
                searchFilters.title,
                date,
                searchFilters.minAmount,
                searchFilters.maxAmount,
                searchFilters.category
            ),
            searchOrder,
            limit
        )
        allTransactions.addAll(transactions)
        searchTransactions.value = allTransactions.distinct()

        // Return the number of transactions returned excluding duplicates.
        return allTransactions.size - numTransactions
    }

    /**
     * Sets the current menu item and updates relevant subscribers.
     *
     * @param menuItem {FooterMenu.MenuItem} The menu item to set.
     */
    fun setCurrentMenuItem(menuItem: FooterMenu.MenuItem) {
        currentMenuItem.value = menuItem
    }

    /**
     * Sets the search transaction filters and updates relevant subscribers.
     *
     * @param filters {SearchFilters} The filters to set.
     */
    fun setSearchTransactionsFilters(filters: SearchFilters) {
        if (filters != searchFilters) {
            searchFilters = filters
            updateSearchTransactions()
        }
    }

    fun getSearchTransactionsFilters(): SearchFilters {
        return searchFilters.copy()
    }

    fun getBudgetGraph(): LiveData<List<BudgetGraph.Point>> {
        return budgetGraph
    }

    fun getTrendsDateRange(): LiveData<OffsetDateTimeRange> {
        return trendsDateRange
    }

    fun setTrendsDateRange(dateRange: OffsetDateTimeRange) {
        trendsDateRange.value = dateRange
        updateTrends()
    }

    private fun updateBudget() {
        currentBudget.value = loadBudget()
    }

    private fun updateRecentTransactions() {
        recentTransactions.value = loadRecentTransactions()
    }

    private fun updateSearchTransactions() {
        searchTransactions.value = loadSearchTransactions()
    }

    private fun updateSumOfTransactions() {
        sumOfRecentTransactions.value = loadSumOfTransactions()
    }

    private fun updateTrends() {
        budgetGraph.value = loadBudgetGraph()
    }

    private fun loadRecentTransactions(): List<Transaction> {
        val filters = SearchFilters(null, recentTransactionsDateRange, null, null, null)
        return dal.transactionDAO.getTransactions(
            filters,
            TransactionDAO.Companion.Order.DATE_DESC,
            4
        )
    }

    private fun loadSearchTransactions(): List<Transaction> {
        return dal.transactionDAO.getTransactions(
            searchFilters,
            searchOrder,
            100
        )
    }

    private fun loadBudget(): Budget? {
        return dal.budgetDAO.getBudgets(recentTransactionsDateRange).getOrNull(0)
    }

    private fun loadSumOfTransactions(): Long {
        val sums = dal.transactionDAO.getSumOfTransactions(recentTransactionsDateRange)
        if (sums.isNotEmpty()) {
            return sums[0].amount
        }
        return 0
    }

    private fun loadBudgetGraph(): List<BudgetGraph.Point> {
        val groupTxnSumsBy: TransactionDAO.Companion.GroupTransactionSums
        val groupBudgetsBy: BudgetDAO.Companion.GroupBudgets

        if (trendsDateRange.value!!.years > 1) {
            groupTxnSumsBy = TransactionDAO.Companion.GroupTransactionSums.YEAR
            groupBudgetsBy = BudgetDAO.Companion.GroupBudgets.YEAR
        } else {
            groupTxnSumsBy = TransactionDAO.Companion.GroupTransactionSums.MONTH
            groupBudgetsBy = BudgetDAO.Companion.GroupBudgets.MONTH
        }

        val budgets = dal.budgetDAO.getBudgets(trendsDateRange.value!!, groupBudgetsBy)
        val transactions =
            dal.transactionDAO.getSumOfTransactions(trendsDateRange.value!!, groupTxnSumsBy)

        val points = mutableListOf<BudgetGraph.Point>()

        for ((i, budget) in budgets.withIndex()) {
            val x: Long = budget?.amount ?: 0
            var y: Long = 0
            val label: String

            for (transaction in transactions) {
                if (
                    groupTxnSumsBy == TransactionDAO.Companion.GroupTransactionSums.MONTH &&
                    transaction.month == trendsDateRange.value!!.startDate!!.monthValue + i
                ) {
                    y = transaction.amount
                } else if (
                    groupTxnSumsBy == TransactionDAO.Companion.GroupTransactionSums.YEAR &&
                    transaction.year == trendsDateRange.value!!.startDate!!.year + i
                ) {
                    y = transaction.amount
                }
            }

            label = if (groupTxnSumsBy == TransactionDAO.Companion.GroupTransactionSums.MONTH) {
                val monthF = DateTimeFormatter.ofPattern("MMM YYYY")
                trendsDateRange.value!!.startDate!!.plusMonths(i.toLong()).format(monthF)
            } else {
                (trendsDateRange.value!!.startDate!!.year + i).toString()
            }

            points.add(BudgetGraph.Point(x, y, label))
        }

        return points
    }
}
