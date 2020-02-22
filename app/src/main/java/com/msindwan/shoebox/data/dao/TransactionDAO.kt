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
package com.msindwan.shoebox.data.dao

import com.msindwan.shoebox.data.entities.*
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId


/**
 * Transaction Data Access Object.
 */
interface TransactionDAO {

    companion object {
        enum class Order(val value: String) {
            DATE_ASC("ASC"),
            DATE_DESC("DESC")
        }

        enum class GroupTransactionSums {
            YEAR,
            MONTH
        }
    }

    /**
     * Inserts a transaction.
     *
     * @param date {LocalDateTime} The datetime of the transaction.
     * @param title {String} The title of the transaction.
     * @param category {String} The transaction category.
     * @param amount {Long} The amount of the transaction in cents.
     * @param currency {Currency} The currency to store the amount in.
     */
    fun insertTransaction(
        date: OffsetDateTime,
        zoneId: ZoneId,
        title: String,
        category: String,
        amount: Long,
        currency: Currency
    ): Transaction

    /**
     * Deletes the transactions provided.
     *
     * @param transactions {List<Transaction>) The list of transactions to delete.
     */
    fun deleteTransactions(transactions: List<Transaction>)

    /**
     * Returns the sum of transactions for the specified date range.
     *
     * @param dateRange {OffsetDateTimeRange} The date range to sum transactions for.
     * @returns {Long} The sum of all transactions found.
     */
    fun getSumOfTransactions(
        dateRange: OffsetDateTimeRange,
        groupBy: GroupTransactionSums = GroupTransactionSums.MONTH
    ): List<TransactionSum>

    /**
     * Returns the transactions for the given search constraints.
     *
     * @param searchFilters {SearchFilters} The search filters to apply.
     * @param order {String} The order to return the results in.
     * @param limit (Int} The limit on the number of results returned.
     * @returns {MutableList<Transaction>} List of transactions found.
     */
    fun getTransactions(
        searchFilters: SearchFilters,
        order: Order = Order.DATE_ASC,
        limit: Int = 100
    ): MutableList<Transaction>
}
