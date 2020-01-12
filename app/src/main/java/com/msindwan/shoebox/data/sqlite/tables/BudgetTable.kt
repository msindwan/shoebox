package com.msindwan.shoebox.data.sqlite.tables

import com.msindwan.shoebox.data.dao.BudgetDAO
import com.msindwan.shoebox.data.sqlite.SQLiteDatabaseHelper
import com.msindwan.shoebox.data.entities.Budget
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class BudgetTable(var dbHelper: SQLiteDatabaseHelper) : BudgetDAO {

    companion object {
        private const val TABLE_NAME = "budget"
        private const val COL_START_DATE = "start_date"
        private const val COL_END_DATE = "end_date"
        private const val COL_AMOUNT = "amount"
        private const val COL_CURRENCY = "currency"
        private const val COL_INTERVAL = "interval"
        private const val COL_DATE_LAST_UPDATED = "date_last_updated"

        fun createTableQuery(): String {
            return """
                CREATE TABLE %s (
                    %s INTEGER NOT NULL,
                    %s INTEGER NOT NULL, 
                    %s INTEGER NOT NULL,
                    %s TEXT NOT NULL default 'USD',
                    %s TEXT NOT NULL default 'M',
                    %s INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (%s, %s)
                )
            """
                .trimIndent()
                .format(
                    TABLE_NAME,
                    COL_START_DATE,
                    COL_END_DATE,
                    COL_AMOUNT,
                    COL_CURRENCY,
                    COL_INTERVAL,
                    COL_DATE_LAST_UPDATED,
                    COL_START_DATE,
                    COL_END_DATE
                )
        }
    }

    override fun getBudget(id: Int): Budget? {
        return Budget(1, 1, 1, "USD", "M", 0)
    }

    override fun getBudgets(startDate: Long, endDate: Long): MutableList<Budget> {
        val db = dbHelper.writableDatabase
        val cBudgets = db.query(
            TABLE_NAME,
            arrayOf(COL_START_DATE, COL_END_DATE, COL_AMOUNT, COL_CURRENCY, COL_INTERVAL, COL_DATE_LAST_UPDATED),
            "%s >= ? AND (%s == -1 OR %s <= ?)".format(COL_START_DATE, COL_END_DATE, COL_END_DATE),
            arrayOf(startDate.toString()),
            null,
            null,
            COL_DATE_LAST_UPDATED
        )

        val budgets: MutableList<Budget> = mutableListOf()

        while (cBudgets?.moveToNext() == true) {
            budgets.add(Budget(
                cBudgets.getLong(cBudgets.getColumnIndex(COL_START_DATE)),
                cBudgets.getLong(cBudgets.getColumnIndex(COL_END_DATE)),
                cBudgets.getLong(cBudgets.getColumnIndex(COL_AMOUNT)),
                cBudgets.getString(cBudgets.getColumnIndex(COL_CURRENCY)),
                cBudgets.getString(cBudgets.getColumnIndex(COL_INTERVAL)),
                cBudgets.getInt(cBudgets.getColumnIndex(COL_DATE_LAST_UPDATED))
            ))
        }
        cBudgets?.close()
        return budgets
    }

    override fun upsertBudget(startDate: Long, endDate: Long, amount: Long, currency: String, interval: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues()
        values.put(COL_START_DATE, startDate)
        values.put(COL_END_DATE, endDate)
        values.put(COL_AMOUNT, amount)
        values.put(COL_CURRENCY, currency)
        values.put(COL_INTERVAL, interval)
        values.put(COL_DATE_LAST_UPDATED, "time('now')")

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    override fun deleteBudget(budget: Budget) {

    }
}
