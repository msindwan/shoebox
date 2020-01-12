package com.msindwan.shoebox.data.dao

import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.data.entities.Transaction

interface TransactionDAO {

    companion object {
        const val ORDER_ASC = "ASC"
        const val ORDER_DESC = "DESC"
    }

    fun getTransaction(transactionId: Int): Transaction
    fun insertTransaction(date: Long, title: String, type: String, amount: Long)
    fun updateTransaction(transaction: Transaction)
    fun deleteTransaction(transaction: Transaction)
    fun getSumOfTransactions(dateRange: DateRange): Int

    fun getTransactions(
        endDate: Long,
        lastCreatedDate: Long?,
        order: String = ORDER_ASC,
        limit: Int = 100
    ): MutableList<Transaction>

    fun getTransactions(
        dateRange: DateRange,
        lastCreatedDate: Int?,
        order: String = ORDER_ASC,
        limit: Int = 100
    ): MutableList<Transaction>
}
