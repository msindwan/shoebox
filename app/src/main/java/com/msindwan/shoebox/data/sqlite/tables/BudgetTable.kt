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
import com.msindwan.shoebox.data.entities.LocalDateRange
import org.threeten.bp.Instant
import org.threeten.bp.Period


/**
 * SQLite BudgetDAO implementation.
 */
class BudgetTable(private val dbHelper: SQLiteDatabaseHelper) : BudgetDAO {

    companion object {
        const val TABLE_NAME = "monthly_budget"
        const val COL_YEAR = "year"
        const val COL_MONTH = "month"
        const val COL_AMOUNT = "amount"
        const val COL_CURRENCY = "currency"
        const val COL_INTERVAL = "interval"
        const val COL_DATE_LAST_UPDATED = "date_last_updated"

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

    override fun getBudgets(dateRange: LocalDateRange, groupBy: BudgetDAO.Companion.GroupBudgets): Array<Budget?> {
        val budgets: MutableList<Budget> = mutableListOf()

        val m1 = dateRange.startDate!!.monthValue.toString()
        val m2 = dateRange.endDate!!.monthValue.toString()
        val y1 = dateRange.startDate.year.toString()
        val y2 = dateRange.endDate.year.toString()

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
                CASE 
                    WHEN $COL_YEAR = ? and $COL_YEAR = ?
                        THEN $COL_MONTH >= ? AND ($COL_MONTH <= ? OR $COL_INTERVAL = 'M')
                    WHEN $COL_YEAR = ?
                        THEN $COL_MONTH >= ? OR $COL_INTERVAL = 'M'
                    WHEN $COL_YEAR = ?
                        THEN $COL_MONTH <= ?
                    WHEN ($COL_YEAR < ? AND $COL_INTERVAL = 'M')
                        THEN 1
                    WHEN ($COL_YEAR > ? AND $COL_YEAR < ?)
                        THEN 1
                    WHEN ($COL_YEAR < ? AND $COL_INTERVAL = 'Y')
                        THEN (? - ? > 1 OR $COL_MONTH >= ? OR $COL_MONTH <= ?)
                    ELSE 0
                END
            """.trimIndent(),
            arrayOf(
                y1,
                y2,
                m1,
                m2,
                y1,
                m1,
                y2,
                m2,
                y1,
                y1,
                y2,
                y2,
                y2,
                y1,
                m1,
                m2
            ),
            null,
            null,
            "$COL_DATE_LAST_UPDATED DESC"
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

        val period = Period.between(dateRange.startDate, dateRange.endDate.plusDays(1))
        val years = period.years + (if (period.months > 0) 1 else 0)
        val months =
            (period.years * 12 + period.months + (if (period.days > 0) 1 else 0)).coerceAtLeast(
                1
            )

        if (groupBy == BudgetDAO.Companion.GroupBudgets.MONTH) {
            val budgetsPerMonth = arrayOfNulls<Budget>(months)

            for (i in 0 until months) {
                val nextMonth = dateRange.startDate.plusMonths(i.toLong())

                for (budget in budgets) {
                    if (
                        (budget.month == nextMonth.monthValue && budget.year == nextMonth.year) ||
                        (budget.month == nextMonth.monthValue && budget.interval == Interval.Y) ||
                        ((i + 1 > budget.month || nextMonth.year > budget.year) && budget.interval == Interval.M)
                    ) {
                        budgetsPerMonth[i] = budget.copy()
                        break
                    }
                }
            }

            return budgetsPerMonth

        }

        val budgetsPerYear = arrayOfNulls<Budget>(years)
        var yIndex = 0
        var year = dateRange.startDate.year

        for (i in 0 until months) {
            val nextMonth = dateRange.startDate.plusMonths(i.toLong())

            if (nextMonth.year != year) {
                year = nextMonth.year
                yIndex++
            }

            for (budget in budgets) {
                if (
                    (budget.month == nextMonth.monthValue && budget.year == nextMonth.year) ||
                    (budget.month == nextMonth.monthValue && budget.interval == Interval.Y) ||
                    ((i + 1 > budget.month || nextMonth.year > budget.year) && budget.interval == Interval.M)
                ) {
                    if (budgetsPerYear[yIndex] == null) {
                        budgetsPerYear[yIndex] = budget.copy()
                    } else {
                        budgetsPerYear[yIndex]!!.amount += budget.amount
                        budgetsPerYear[yIndex]!!.interval = Interval.N
                    }
                    break
                }
            }
        }

        return budgetsPerYear
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
