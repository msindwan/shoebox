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

    private val trendsBudgetGraph: MutableLiveData<List<BudgetGraph.Point>> by lazy {
        MutableLiveData(loadBudgetGraph())
    }

    private var trendsDateRange: MutableLiveData<OffsetDateTimeRange> =
        MutableLiveData(OffsetDateTimeRange.currentMonth().minusStart(7, ChronoUnit.MONTHS))

    private var recentTransactionsDateRange: OffsetDateTimeRange =
        OffsetDateTimeRange.currentMonth()

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
    fun insertTransaction(
        date: OffsetDateTime,
        zoneId: ZoneId,
        title: String,
        category: String,
        amount: Long
    ) {
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

    /**
     * Returns current search transaction filters.
     *
     * @returns {SearchFilters} The current filters set.
     */
    fun getSearchTransactionsFilters(): SearchFilters {
        return searchFilters.copy()
    }

    /**
     * Returns the data points for the trends budget graph.
     *
     * @return {LiveData} The points for the graph.
     */
    fun getTrendsBudgetGraph(): LiveData<List<BudgetGraph.Point>> {
        return trendsBudgetGraph
    }

    /**
     * Returns the date range for trends.
     *
     * @return {LiveData} The points for the graph.
     */
    fun getTrendsDateRange(): LiveData<OffsetDateTimeRange> {
        return trendsDateRange
    }

    /**
     * Sets the date range for trends and updates relevant subscribers.
     *
     * @param dateRange {OffsetDateTime} The updated trends date range.
     */
    fun setTrendsDateRange(dateRange: OffsetDateTimeRange) {
        trendsDateRange.value = dateRange
        updateTrends()
    }

    /**
     * Refreshes the current budget.
     */
    private fun updateBudget() {
        currentBudget.value = loadBudget()
    }

    /**
     * Refreshes the recent transactions.
     */
    private fun updateRecentTransactions() {
        recentTransactions.value = loadRecentTransactions()
    }

    /**
     * Refreshes the search transactions.
     */
    private fun updateSearchTransactions() {
        searchTransactions.value = loadSearchTransactions()
    }

    /**
     * Refreshes the sum of transactions.
     */
    private fun updateSumOfTransactions() {
        sumOfRecentTransactions.value = loadSumOfTransactions()
    }

    /**
     * Refreshes the trends budget graph.
     */
    private fun updateTrends() {
        trendsBudgetGraph.value = loadBudgetGraph()
    }

    /**
     * Fetches recent transactions by date range.
     *
     * @returns {List<Transaction>} A list of recent transactions.
     */
    private fun loadRecentTransactions(): List<Transaction> {
        val filters = SearchFilters(null, recentTransactionsDateRange, null, null, null)
        return dal.transactionDAO.getTransactions(
            filters,
            TransactionDAO.Companion.Order.DATE_DESC,
            4
        )
    }

    /**
     * Fetches search transactions by filters.
     *
     * @returns {List<Transaction>} A list of transactions matching the search criteria.
     */
    private fun loadSearchTransactions(): List<Transaction> {
        return dal.transactionDAO.getTransactions(
            searchFilters,
            searchOrder,
            100
        )
    }

    /**
     * Fetches the budget for the current date range.
     *
     * @returns {Budget?} The budget for the current date range.
     */
    private fun loadBudget(): Budget? {
        return dal.budgetDAO.getBudgets(recentTransactionsDateRange).getOrNull(0)
    }

    /**
     * Fetches the sum of transactions for the current date range.
     *
     * @returns {Long} The sum of transactions.
     */
    private fun loadSumOfTransactions(): Long {
        val sums = dal.transactionDAO.getSumOfTransactions(recentTransactionsDateRange)
        return sums.getOrNull(0)?.amount ?: 0
    }

    /**
     * Loads the budget graph for the trends date range.
     *
     * @returns {List<BudgetGraph.Point>} The list of points.
     */
    private fun loadBudgetGraph(): List<BudgetGraph.Point> {
        val groupTxnSumsBy: TransactionDAO.Companion.GroupTransactionSums
        val groupBudgetsBy: BudgetDAO.Companion.GroupBudgets
        val monthFormatter = DateTimeFormatter.ofPattern("MMM YYYY")
        val points = mutableListOf<BudgetGraph.Point>()
        val xTicks: Long

        // Get budget graph attributes based on date range.
        if (trendsDateRange.value!!.years > 1) {
            groupTxnSumsBy = TransactionDAO.Companion.GroupTransactionSums.YEAR
            groupBudgetsBy = BudgetDAO.Companion.GroupBudgets.YEAR
            xTicks = trendsDateRange.value!!.years
        } else {
            groupTxnSumsBy = TransactionDAO.Companion.GroupTransactionSums.MONTH
            groupBudgetsBy = BudgetDAO.Companion.GroupBudgets.MONTH
            xTicks = trendsDateRange.value!!.months
        }

        // Fetch budgets and transactions for the date range grouped by month or year.
        val budgets = dal.budgetDAO.getBudgets(trendsDateRange.value!!, groupBudgetsBy)
        val transactions =
            dal.transactionDAO.getSumOfTransactions(trendsDateRange.value!!, groupTxnSumsBy)

        for (i in 0 until xTicks) {
            // Derive budget and transaction per tick.
            val budget: Budget?
            val sum: TransactionSum?
            val label: String

            if (groupBudgetsBy == BudgetDAO.Companion.GroupBudgets.YEAR) {
                budget = budgets.filterNotNull().find { b ->
                    b.year == trendsDateRange.value!!.startDate?.year?.plus(i.toInt())
                }
                sum = transactions.find { t -> t.year == budget?.year }
                label = trendsDateRange.value!!.startDate!!.year.plus(i).toString()
            } else {
                budget = budgets.filterNotNull().find { b ->
                    b.year == trendsDateRange.value!!.startDate?.plusMonths(i)?.year &&
                            b.month == trendsDateRange.value!!.startDate?.plusMonths(i)?.monthValue
                }
                sum = transactions.find { t -> t.month == budget?.month && t.year == budget?.year }
                label = trendsDateRange.value!!.startDate!!.plusMonths(i).format(monthFormatter)
            }

            points.add(BudgetGraph.Point(budget?.amount ?: 0, sum?.amount ?: 0, label))
        }

        return points
    }

}
