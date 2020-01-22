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
package com.msindwan.shoebox.data

import android.content.Context
import com.msindwan.shoebox.data.dao.BudgetDAO
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.sqlite.SQLiteDatabaseHelper
import com.msindwan.shoebox.data.sqlite.tables.BudgetTable
import com.msindwan.shoebox.data.sqlite.tables.TransactionTable


/**
 * DAL singleton instance for the application.
 */
class DataAccessLayer private constructor(context: Context) {
    var budgetDAO: BudgetDAO
    var transactionDAO: TransactionDAO

    init {
        val sqliteDriver = SQLiteDatabaseHelper(context)
        budgetDAO = BudgetTable(sqliteDriver)
        transactionDAO = TransactionTable(sqliteDriver)
    }

    companion object {
        @Volatile
        private var sqliteDAL: DataAccessLayer? = null

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