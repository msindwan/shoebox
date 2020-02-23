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
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import java.util.*


/**
 * SQLite TransactionDAO implementation.
 */
class TransactionTable(private val dbHelper: SQLiteDatabaseHelper) : TransactionDAO {

    companion object {
        private const val TABLE_NAME = "payment_transaction"
        private const val COL_TRANSACTION_ID = "transaction_id"
        private const val COL_LOCAL_TIMESTAMP = "local_timestamp"
        private const val COL_UTC_TIMESTAMP = "utc_timestamp"
        private const val COL_TIMEZONE = "timezone"
        private const val COL_TITLE = "title"
        private const val COL_CATEGORY = "category"
        private const val COL_AMOUNT = "amount"
        private const val COL_CURRENCY = "currency"
        private const val COL_TIME_CREATED = "time_created"

        fun createTableQuery(): String {
            return """
                CREATE TABLE $TABLE_NAME (
                    $COL_TRANSACTION_ID BLOB NOT NULL PRIMARY KEY,
                    $COL_LOCAL_TIMESTAMP TEXT NOT NULL, 
                    $COL_UTC_TIMESTAMP INTEGER NOT NULL,
                    $COL_TIMEZONE TEXT NOT NULL,
                    $COL_TITLE TEXT,
                    $COL_CATEGORY TEXT NOT NULL,
                    $COL_AMOUNT INTEGER NOT NULL,
                    $COL_CURRENCY TEXT NOT NULL default 'USD',
                    $COL_TIME_CREATED INTEGER NOT NULL 
                )
            """
                .trimIndent()
        }
    }

    override fun insertTransaction(
        date: OffsetDateTime,
        zoneId: ZoneId,
        title: String,
        category: String,
        amount: Long,
        currency: Currency
    ): Transaction {
        val db = dbHelper.writableDatabase
        val uuid = UUIDHelpers.getBytesFromUUID(UUID.randomUUID())
        val now = Instant.now()

        val values = ContentValues()
        values.put(COL_TRANSACTION_ID, uuid)
        values.put(COL_LOCAL_TIMESTAMP, date.toString())
        values.put(COL_UTC_TIMESTAMP, date.toEpochSecond())
        values.put(COL_TIMEZONE, zoneId.id)
        values.put(COL_TITLE, title)
        values.put(COL_AMOUNT, amount)
        values.put(COL_CATEGORY, category)
        values.put(COL_CURRENCY, currency.code)
        values.put(COL_TIME_CREATED, now.toEpochMilli())

        db.insert(TABLE_NAME, null, values)
        return Transaction(uuid, date, zoneId, title, category, amount, currency, now)
    }

    override fun deleteTransactions(transactions: List<Transaction>) {
        if (transactions.isNotEmpty()) {
            val db = dbHelper.writableDatabase
            val ids = transactions.joinToString(" OR ") { "$COL_TRANSACTION_ID = ?" }

            db.execSQL(
                "DELETE FROM $TABLE_NAME WHERE $ids",
                transactions.map { t -> t.id }.toTypedArray()
            )
        }
    }

    override fun getTransactions(
        searchFilters: SearchFilters,
        order: TransactionDAO.Companion.Order,
        limit: Int
    ): MutableList<Transaction> {
        val db = dbHelper.writableDatabase

        val transactions: MutableList<Transaction> = mutableListOf()
        val selection: MutableList<String> = mutableListOf()
        val selectionArgs: MutableList<String> = mutableListOf()

        if (searchFilters.dateRange.startDate != null) {
            selection.add("$COL_UTC_TIMESTAMP >= ?")
            selectionArgs.add(searchFilters.dateRange.startDate!!.toEpochSecond().toString())
        }
        if (searchFilters.dateRange.endDate != null) {
            selection.add("$COL_UTC_TIMESTAMP <= ?")
            selectionArgs.add(searchFilters.dateRange.endDate!!.toEpochSecond().toString())
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
                COL_LOCAL_TIMESTAMP,
                COL_UTC_TIMESTAMP,
                COL_TIMEZONE,
                COL_TITLE,
                COL_CATEGORY,
                COL_AMOUNT,
                COL_CURRENCY,
                COL_TIME_CREATED
            ),
            selection.joinToString(" AND "),
            selectionArgs.toTypedArray(),
            null,
            null,
            "$COL_UTC_TIMESTAMP ${order.value}, $COL_TIME_CREATED ${order.value}",
            limit.toString()
        )

        if (cTransactions != null) {
            while (cTransactions.moveToNext()) {
                transactions.add(
                    Transaction(
                        cTransactions.getBlob(cTransactions.getColumnIndex(COL_TRANSACTION_ID)),
                        OffsetDateTime.parse(
                            cTransactions.getString(
                                cTransactions.getColumnIndex(
                                    COL_LOCAL_TIMESTAMP
                                )
                            )
                        ),
                        ZoneId.of(
                            cTransactions.getString(
                                cTransactions.getColumnIndex(
                                    COL_TIMEZONE
                                )
                            )
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
                            cTransactions.getLong(
                                cTransactions.getColumnIndex(
                                    COL_TIME_CREATED
                                )
                            )
                        )
                    )
                )
            }
        }

        cTransactions?.close()
        return transactions
    }

    override fun getSumOfTransactions(
        dateRange: OffsetDateTimeRange,
        groupBy: TransactionDAO.Companion.GroupTransactionSums
    ): List<TransactionSum> {
        val db = dbHelper.writableDatabase
        val totals = mutableListOf<TransactionSum>()

        val dateFilter = if (dateRange.startDate == null || dateRange.endDate == null) {
            OffsetDateTimeRange.currentMonth()
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
                    strftime('%Y', datetime($COL_UTC_TIMESTAMP, 'unixepoch', 'localtime')) as Year,
                    strftime('%m', datetime($COL_UTC_TIMESTAMP, 'unixepoch', 'localtime')) as Month
                FROM
                    $TABLE_NAME
                WHERE 
                    $COL_UTC_TIMESTAMP >= ? AND $COL_UTC_TIMESTAMP <= ?
                $groupByClause
                ORDER BY
                    $COL_UTC_TIMESTAMP ASC
            """.trimIndent(),
            arrayOf(
                dateFilter.startDate!!.toEpochSecond().toString(),
                dateFilter.endDate!!.toEpochSecond().toString()
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
