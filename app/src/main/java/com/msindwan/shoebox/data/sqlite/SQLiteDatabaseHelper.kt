package com.msindwan.shoebox.data.sqlite

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.msindwan.shoebox.data.sqlite.tables.BudgetTable
import com.msindwan.shoebox.data.sqlite.tables.TransactionTable

const val DATABASE_NAME = "shoebox"
const val DATABASE_VERSION = 1

class SQLiteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys=ON")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(BudgetTable.createTableQuery())
        db.execSQL(TransactionTable.createTableQuery())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}
