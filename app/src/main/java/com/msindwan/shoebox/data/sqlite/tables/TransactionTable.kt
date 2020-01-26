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

import android.content.ContentValues
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.entities.*
import com.msindwan.shoebox.data.entities.Currency
import com.msindwan.shoebox.data.sqlite.SQLiteDatabaseHelper
import com.msindwan.shoebox.helpers.UUIDHelpers
import java.util.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate


/**
 * SQLite TransactionDAO implementation.
 */
class TransactionTable(private val dbHelper: SQLiteDatabaseHelper) : TransactionDAO {

    companion object {
        private const val TABLE_NAME = "payment_transaction"
        private const val COL_TRANSACTION_ID = "transaction_id"
        private const val COL_DATE = "date"
        private const val COL_TITLE = "title"
        private const val COL_CATEGORY = "category"
        private const val COL_AMOUNT = "amount"
        private const val COL_CURRENCY = "currency"
        private const val COL_DATE_CREATED = "date_created"

        fun createTableQuery(): String {
            return """
                CREATE TABLE $TABLE_NAME (
                    $COL_TRANSACTION_ID BLOB NOT NULL PRIMARY KEY,
                    $COL_DATE INTEGER NOT NULL, 
                    $COL_TITLE TEXT,
                    $COL_CATEGORY TEXT NOT NULL,
                    $COL_AMOUNT INTEGER NOT NULL,
                    $COL_CURRENCY TEXT NOT NULL default 'USD',
                    $COL_DATE_CREATED INTEGER NOT NULL
                )
            """
                .trimIndent()
        }
    }

    override fun insertTransaction(
        date: LocalDate,
        title: String,
        category: String,
        amount: Long,
        currency: Currency
    ): Transaction {
        val db = dbHelper.writableDatabase
        val uuid = UUIDHelpers.getBytesFromUUID(UUID.randomUUID())
        val dateCreated = Instant.now()

        val values = ContentValues()
        values.put(COL_TRANSACTION_ID, uuid)
        values.put(COL_DATE, date.toEpochDay())
        values.put(COL_TITLE, title)
        values.put(COL_AMOUNT, amount)
        values.put(COL_CATEGORY, category)
        values.put(COL_CURRENCY, currency.code)
        values.put(COL_DATE_CREATED, dateCreated.toEpochMilli())

        db.insert(TABLE_NAME, null, values)
        return Transaction(uuid, date, title, category, amount, currency, dateCreated)
    }

    override fun deleteTransactions(transactions: List<Transaction>) {
        if (transactions.isNotEmpty()) {
            val ids =
                transactions.joinToString(" OR ", "", "", -1, "...") { "$COL_TRANSACTION_ID = ?" }
            val db = dbHelper.writableDatabase

            db.execSQL(
                "DELETE FROM $TABLE_NAME WHERE $ids",
                transactions.map { t -> t.id }.toTypedArray()
            )
        }
    }

    override fun getTransactions(
        searchFilters: SearchFilters,
        lastCreatedDate: Instant?,
        order: TransactionDAO.Companion.Order,
        limit: Int
    ): MutableList<Transaction> {
        val db = dbHelper.writableDatabase

        val transactions: MutableList<Transaction> = mutableListOf()
        val selection: MutableList<String> = mutableListOf()
        val selectionArgs: MutableList<String> = mutableListOf()

        if (lastCreatedDate != null) {
            selection.add("$COL_DATE_CREATED <= ?")
            selectionArgs.add(lastCreatedDate.toEpochMilli().toString())
        }
        if (searchFilters.dateRange.startDate != null) {
            selection.add("$COL_DATE >= ?")
            selectionArgs.add(searchFilters.dateRange.startDate!!.toEpochDay().toString())
        }
        if (searchFilters.dateRange.endDate != null) {
            selection.add("$COL_DATE <= ?")
            selectionArgs.add(searchFilters.dateRange.endDate!!.toEpochDay().toString())
        }
        if (searchFilters.title != null) {
            selection.add("$COL_TITLE LIKE ?")
            selectionArgs.add("%${searchFilters.title!!}%")
        }
        if (searchFilters.category != null) {
            selection.add("$COL_CATEGORY LIKE ?")
            selectionArgs.add("%${searchFilters.category!!}%")
        }
        if (searchFilters.maxAmount != null) {
            selection.add("$COL_AMOUNT <= ?")
            selectionArgs.add(searchFilters.maxAmount!!.toString())
        }
        if (searchFilters.minAmount != null) {
            selection.add("$COL_AMOUNT >= ?")
            selectionArgs.add(searchFilters.minAmount!!.toString())
        }

        val cTransactions = db.query(
            TABLE_NAME,
            arrayOf(
                COL_TRANSACTION_ID,
                COL_DATE,
                COL_TITLE,
                COL_CATEGORY,
                COL_AMOUNT,
                COL_CURRENCY,
                COL_DATE_CREATED
            ),
            selection.joinToString(" AND "),
            selectionArgs.toTypedArray(),
            null,
            null,
            "$COL_DATE ${order.value}, $COL_DATE_CREATED ${order.value}",
            limit.toString()
        )

        if (cTransactions != null) {
            while (cTransactions.moveToNext()) {
                // read
                transactions.add(
                    Transaction(
                        cTransactions.getBlob(cTransactions.getColumnIndex(COL_TRANSACTION_ID)),
                        LocalDate.ofEpochDay(
                            cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE))
                        ),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TITLE)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_CATEGORY)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_AMOUNT)),
                        Currency.valueOf(
                            cTransactions.getString(
                                cTransactions.getColumnIndex(
                                    COL_CURRENCY
                                )
                            )
                        ),
                        Instant.ofEpochMilli(
                            cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE_CREATED))
                        )
                    )
                )
            }
        }

        cTransactions?.close()
        return transactions
    }

    override fun getSumOfTransactions(
        dateRange: LocalDateRange,
        groupBy: TransactionDAO.Companion.GroupTransactionSums
    ): List<TransactionSum> {
        val db = dbHelper.writableDatabase
        val totals = mutableListOf<TransactionSum>()

        val dateFilter = if (dateRange.startDate == null || dateRange.endDate == null) {
            LocalDateRange.currentMonth()
        } else {
            dateRange
        }

        val groupByClause = if (groupBy == TransactionDAO.Companion.GroupTransactionSums.MONTH) {
            "GROUP BY Year, Month"
        } else {
            "GROUP BY Year"
        }

        val cursor = db.rawQuery(
            """
                SELECT
                    SUM($COL_AMOUNT) as Total,
                    strftime('%Y', datetime($COL_DATE * 86400, 'unixepoch', 'localtime')) as Year,
                    strftime('%m', datetime($COL_DATE * 86400, 'unixepoch', 'localtime')) as Month
                FROM
                    $TABLE_NAME
                WHERE 
                    $COL_DATE >= ? AND $COL_DATE <= ?
                $groupByClause
                ORDER BY
                    $COL_DATE ASC
            """.trimIndent(),
            arrayOf(
                dateFilter.startDate!!.toEpochDay().toString(),
                dateRange.endDate!!.toEpochDay().toString()
            )
        )

        while (cursor.moveToNext()) {
            totals.add(
                TransactionSum(
                    cursor.getLong(cursor.getColumnIndex("Total")),
                    cursor.getInt(cursor.getColumnIndex("Year")),
                    cursor.getInt(cursor.getColumnIndex("Month"))
                )
            )
        }

        cursor?.close()
        return totals
    }
}
