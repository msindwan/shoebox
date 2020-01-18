package com.msindwan.shoebox.data.sqlite.tables

import android.content.ContentValues
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.data.entities.Transaction
import com.msindwan.shoebox.data.sqlite.SQLiteDatabaseHelper
import com.msindwan.shoebox.helpers.UUIDHelpers
import java.util.*
import android.text.TextUtils
import com.msindwan.shoebox.data.entities.SearchFilters


class TransactionTable(var dbHelper: SQLiteDatabaseHelper) : TransactionDAO {

    companion object {
        private const val TABLE_NAME = "payment_transaction"
        private const val COL_TRANSACTION_ID = "transaction_id"
        private const val COL_DATE = "date"
        private const val COL_TITLE = "title"
        private const val COL_AMOUNT = "amount"
        private const val COL_TYPE = "type"
        private const val COL_DATE_CREATED = "date_created"

        fun createTableQuery(): String {
            return """
                CREATE TABLE $TABLE_NAME (
                    $COL_TRANSACTION_ID BLOB NOT NULL PRIMARY KEY,
                    $COL_DATE INTEGER NOT NULL, 
                    $COL_TITLE TEXT,
                    $COL_AMOUNT INTEGER NOT NULL,
                    $COL_TYPE TEXT NOT NULL,
                    $COL_DATE_CREATED INTEGER NOT NULL
                )
            """
                .trimIndent()
        }
    }

    override fun getTransaction(transactionId: Int): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertTransaction(date: Long, title: String, type: String, amount: Long) : Transaction {
        val db = dbHelper.writableDatabase
        val uuid = UUIDHelpers.getBytesFromUUID(UUID.randomUUID())
        val dateCreated = Date().time

        val values = ContentValues()
        values.put(COL_TRANSACTION_ID, uuid)
        values.put(COL_DATE, date)
        values.put(COL_TITLE, title)
        values.put(COL_AMOUNT, amount)
        // @todo Use enum for type
        values.put(COL_TYPE, type)
        values.put(COL_DATE_CREATED, dateCreated)

        db.insert(TABLE_NAME, null, values)
        return Transaction(uuid, date, title, amount, type, dateCreated)
    }

    override fun updateTransaction(transaction: Transaction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteTransactions(transactions: List<Transaction>) {
        if (transactions.isNotEmpty()) {
            val ids =
                transactions.joinToString(" OR ", "", "", -1, "...") { "$COL_TRANSACTION_ID = ?" }
            val db = dbHelper.writableDatabase

            db.execSQL("DELETE FROM $TABLE_NAME WHERE $ids", transactions.map { t -> t.id }.toTypedArray())
        }
    }

    override fun getTransactions(
        searchFilters: SearchFilters,
        lastCreatedDate: Long?,
        order: String,
        limit: Int
    ): MutableList<Transaction> {
        val db = dbHelper.writableDatabase

        val transactions: MutableList<Transaction> = mutableListOf()
        val selection: MutableList<String> = mutableListOf()
        val selectionArgs: MutableList<String> = mutableListOf()

        if (lastCreatedDate != null) {
            selection.add("$COL_DATE_CREATED <= ?")
            selectionArgs.add(lastCreatedDate.toString())
        }
        if (searchFilters.dateRange.startDate != DateRange.NO_START_DATE) {
            selection.add("$COL_DATE >= ?")
            selectionArgs.add(searchFilters.dateRange.startDate.toString())
        }
        if (searchFilters.dateRange.endDate != DateRange.NO_END_DATE) {
            selection.add("$COL_DATE <= ?")
            selectionArgs.add(searchFilters.dateRange.endDate.toString())
        }
        if (searchFilters.title != null) {
            selection.add("$COL_TITLE LIKE ?")
            selectionArgs.add("%${searchFilters.title!!}%")
        }
        if (searchFilters.category != null) {
            selection.add("$COL_TYPE LIKE ?")
            selectionArgs.add("%${searchFilters.category!!}%")
        }

        val cTransactions = db.query(
            TABLE_NAME,
            arrayOf(
                COL_TRANSACTION_ID,
                COL_DATE,
                COL_TITLE,
                COL_AMOUNT,
                COL_TYPE,
                COL_DATE_CREATED
            ),
            selection.joinToString(" AND "),
            selectionArgs.toTypedArray(),
            null,
            null,
            "$COL_DATE $order",
            limit.toString()
        )

        if (cTransactions != null) {
            while (cTransactions.moveToNext()) {
                // read
                transactions.add(
                    Transaction(
                        cTransactions.getBlob(cTransactions.getColumnIndex(COL_TRANSACTION_ID)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TITLE)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_AMOUNT)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TYPE)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE_CREATED))
                    )
                )
            }
        }

        cTransactions?.close()
        return transactions
    }

    /*override fun getTransactions(
        endDate: Long,
        lastCreatedDate: Long?,
        order: String,
        limit: Int
    ): MutableList<Transaction> {
        val db = dbHelper.writableDatabase

        val transactions: MutableList<Transaction> = mutableListOf()
        val selection: String
        val selectionArgs: Array<String>

        if (lastCreatedDate != null) {
            selection = "$COL_DATE <= ? AND $COL_DATE_CREATED <= ?"
            selectionArgs = arrayOf(endDate.toString(), lastCreatedDate.toString())
        } else {
            selection = "$COL_DATE <= ?"
            selectionArgs = arrayOf(endDate.toString())
        }

        val cTransactions = db.query(
            TABLE_NAME,
            arrayOf(
                COL_TRANSACTION_ID,
                COL_DATE,
                COL_TITLE,
                COL_AMOUNT,
                COL_TYPE,
                COL_DATE_CREATED
            ),
            selection,
            selectionArgs,
            null,
            null,
            "$COL_DATE $order",
            limit.toString()
        )

        if (cTransactions != null) {
            while (cTransactions.moveToNext()) {
                // read
                transactions.add(
                    Transaction(
                        cTransactions.getBlob(cTransactions.getColumnIndex(COL_TRANSACTION_ID)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TITLE)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_AMOUNT)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TYPE)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE_CREATED))
                    )
                )
            }
        }

        cTransactions?.close()
        return transactions
    }

    override fun getTransactions(
        lastCreatedDate: Long?,
        order: String,
        limit: Int
    ): MutableList<Transaction> {
        val db = dbHelper.writableDatabase

        val transactions: MutableList<Transaction> = mutableListOf()
        var selection: String? = null
        var selectionArgs: Array<String>? = null

        if (lastCreatedDate != null) {
            selection = "$COL_DATE_CREATED <= ?"
            selectionArgs = arrayOf(lastCreatedDate.toString())
        }

        val cTransactions = db.query(
            TABLE_NAME,
            arrayOf(
                COL_TRANSACTION_ID,
                COL_DATE,
                COL_TITLE,
                COL_AMOUNT,
                COL_TYPE,
                COL_DATE_CREATED
            ),
            selection,
            selectionArgs,
            null,
            null,
            "$COL_DATE $order",
            limit.toString()
        )

        if (cTransactions != null) {
            while (cTransactions.moveToNext()) {
                // read
                transactions.add(
                    Transaction(
                        cTransactions.getBlob(cTransactions.getColumnIndex(COL_TRANSACTION_ID)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TITLE)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_AMOUNT)),
                        cTransactions.getString(cTransactions.getColumnIndex(COL_TYPE)),
                        cTransactions.getLong(cTransactions.getColumnIndex(COL_DATE_CREATED))
                    )
                )
            }
        }

        cTransactions?.close()
        return transactions
    }*/

    override fun getSumOfTransactions(dateRange: DateRange): Long {
        val db = dbHelper.writableDatabase
        var total = 0L
        val cursor = db.rawQuery(
            """
                SELECT SUM($COL_AMOUNT) as Total FROM $TABLE_NAME WHERE $COL_DATE >= ? AND $COL_DATE <= ?
            """.trimIndent(),
            arrayOf(dateRange.startDate.toString(), dateRange.endDate.toString())
        );

        if (cursor.moveToFirst()) {
            total = cursor.getLong(cursor.getColumnIndex("Total"))
        }

        cursor?.close()
        return total
    }
}