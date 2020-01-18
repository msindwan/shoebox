package com.msindwan.shoebox.data.dao

import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.data.entities.SearchFilters
import com.msindwan.shoebox.data.entities.Transaction

interface TransactionDAO {

    companion object {
        const val ORDER_ASC = "ASC"
        const val ORDER_DESC = "DESC"
    }

    fun getTransaction(transactionId: Int): Transaction
    fun insertTransaction(date: Long, title: String, type: String, amount: Long) : Transaction
    fun updateTransaction(transaction: Transaction)
    fun deleteTransactions(transactions: List<Transaction>)
    fun getSumOfTransactions(dateRange: DateRange): Long

    fun getTransactions(
        searchFilters: SearchFilters,
        lastCreatedDate: Long?,
        order: String = ORDER_ASC,
        limit: Int = 100
    ): MutableList<Transaction>
}
