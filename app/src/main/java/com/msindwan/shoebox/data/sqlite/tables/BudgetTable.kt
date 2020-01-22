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
package com.msindwan.shoebox.data.sqlite.tables

import com.msindwan.shoebox.data.dao.BudgetDAO
import com.msindwan.shoebox.data.sqlite.SQLiteDatabaseHelper
import com.msindwan.shoebox.data.entities.Budget
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.msindwan.shoebox.data.entities.Currency
import com.msindwan.shoebox.data.entities.Interval
import org.threeten.bp.Instant


/**
 * SQLite BudgetDAO implementation.
 */
class BudgetTable(private val dbHelper: SQLiteDatabaseHelper) : BudgetDAO {

    companion object {
        private const val TABLE_NAME = "monthly_budget"
        private const val COL_YEAR = "year"
        private const val COL_MONTH = "month"
        private const val COL_AMOUNT = "amount"
        private const val COL_CURRENCY = "currency"
        private const val COL_INTERVAL = "interval"
        private const val COL_DATE_LAST_UPDATED = "date_last_updated"

        fun createTableQuery(): String {
            return """
                CREATE TABLE $TABLE_NAME (
                    $COL_MONTH INTEGER NOT NULL,
                    $COL_YEAR INTEGER NOT NULL,
                    $COL_INTERVAL TEXT NOT NULL default 'N',
                    $COL_AMOUNT INTEGER NOT NULL,
                    $COL_CURRENCY TEXT NOT NULL default 'USD',
                    $COL_DATE_LAST_UPDATED INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY ($COL_MONTH, $COL_YEAR, $COL_INTERVAL)
                )
            """
                .trimIndent()
        }
    }

    override fun getBudgetForMonth(month: Int, year: Int): Budget? {
        val db = dbHelper.writableDatabase
        val cBudgets = db.query(
            TABLE_NAME,
            arrayOf(
                COL_MONTH,
                COL_YEAR,
                COL_INTERVAL,
                COL_AMOUNT,
                COL_CURRENCY,
                COL_DATE_LAST_UPDATED
            ),
            """
                ($COL_MONTH = ? AND $COL_YEAR = ?) OR 
                ($COL_YEAR < ? OR ($COL_YEAR = ? AND $COL_MONTH < ?) AND $COL_INTERVAL = 'M') OR
                ($COL_MONTH = ? AND $COL_YEAR > ? AND $COL_INTERVAL = 'Y')
            """.trimIndent(),
            arrayOf(
                month.toString(),
                year.toString(),
                year.toString(),
                year.toString(),
                month.toString(),
                month.toString(),
                year.toString()
            ),
            null,
            null,
            "$COL_DATE_LAST_UPDATED ${BudgetDAO.Companion.Order.DATE_DESC.value}"
        )

        var budget: Budget? = null

        if (cBudgets?.moveToFirst() == true) {
            budget = Budget(
                cBudgets.getInt(cBudgets.getColumnIndex(COL_MONTH)),
                cBudgets.getInt(cBudgets.getColumnIndex(COL_YEAR)),
                Interval.valueOf(cBudgets.getString(cBudgets.getColumnIndex(COL_INTERVAL))),
                cBudgets.getLong(cBudgets.getColumnIndex(COL_AMOUNT)),
                Currency.valueOf(cBudgets.getString(cBudgets.getColumnIndex(COL_CURRENCY))),
                Instant.ofEpochMilli(cBudgets.getLong(cBudgets.getColumnIndex(COL_DATE_LAST_UPDATED)))
            )
        }
        cBudgets?.close()
        return budget
    }

    override fun getBudgetsForYear(year: Int, order: BudgetDAO.Companion.Order): List<Budget> {
        val budgets: MutableList<Budget> = mutableListOf()

        val db = dbHelper.writableDatabase
        val cBudgets = db.query(
            TABLE_NAME,
            arrayOf(
                COL_MONTH,
                COL_YEAR,
                COL_INTERVAL,
                COL_AMOUNT,
                COL_CURRENCY,
                COL_DATE_LAST_UPDATED
            ),
            """
                ($COL_YEAR = ?) OR
                ($COL_YEAR < ? AND ($COL_INTERVAL = 'M' OR $COL_INTERVAL = 'Y'))
            """.trimIndent(),
            arrayOf(
                year.toString(),
                year.toString()
            ),
            null,
            null,
            "$COL_DATE_LAST_UPDATED ${order.value}"
        )

        while (cBudgets?.moveToNext() == true) {
            budgets.add(
                Budget(
                    cBudgets.getInt(cBudgets.getColumnIndex(COL_MONTH)),
                    cBudgets.getInt(cBudgets.getColumnIndex(COL_YEAR)),
                    Interval.valueOf(cBudgets.getString(cBudgets.getColumnIndex(COL_INTERVAL))),
                    cBudgets.getLong(cBudgets.getColumnIndex(COL_AMOUNT)),
                    Currency.valueOf(cBudgets.getString(cBudgets.getColumnIndex(COL_CURRENCY))),
                    Instant.ofEpochMilli(
                        cBudgets.getLong(
                            cBudgets.getColumnIndex(
                                COL_DATE_LAST_UPDATED
                            )
                        )
                    )
                )
            )
        }
        cBudgets?.close()
        return budgets
    }

    override fun upsertBudget(
        month: Int,
        year: Int,
        interval: Interval,
        amount: Long,
        currency: Currency
    ) {
        val db = dbHelper.writableDatabase
        val dateCreated = Instant.now()

        val values = ContentValues()
        values.put(COL_MONTH, month)
        values.put(COL_YEAR, year)
        values.put(COL_INTERVAL, interval.value)
        values.put(COL_AMOUNT, amount)
        values.put(COL_CURRENCY, currency.code)
        values.put(COL_DATE_LAST_UPDATED, dateCreated.toEpochMilli())

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
