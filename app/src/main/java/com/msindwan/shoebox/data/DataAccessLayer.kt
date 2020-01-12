package com.msindwan.shoebox.data

import android.content.Context
import com.msindwan.shoebox.data.dao.BudgetDAO
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.sqlite.SQLiteDatabaseHelper
import com.msindwan.shoebox.data.sqlite.tables.BudgetTable
import com.msindwan.shoebox.data.sqlite.tables.TransactionTable

class DataAccessLayer private constructor(context: Context) {
    var budgetDAO: BudgetDAO? = null
    var transactionDAO: TransactionDAO? = null

    init {
        val sqliteDriver = SQLiteDatabaseHelper(context)
        budgetDAO = BudgetTable(sqliteDriver)
        transactionDAO = TransactionTable(sqliteDriver)
    }

    companion object {
        @Volatile private var sqliteDAL: DataAccessLayer? = null

        fun getInstance(context: Context): DataAccessLayer {
            val checkInstance = sqliteDAL
            if (checkInstance != null) {
                return checkInstance
            }

            return synchronized(this) {
                val checkInstanceAgain = sqliteDAL
                if (checkInstanceAgain != null) {
                    checkInstanceAgain
                } else {
                    val created = DataAccessLayer(context)
                    sqliteDAL = created
                    created
                }
            }
        }
    }
}