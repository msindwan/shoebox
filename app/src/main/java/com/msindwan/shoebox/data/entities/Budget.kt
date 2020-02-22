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
package com.msindwan.shoebox.data.entities

import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

data class Budget(
    var month: Int,
    var year: Int,
    var interval: Interval,
    var amount: Long,
    var currency: Currency,
    var dateLastUpdated: Instant
) {
    /**
     * Returns whether or not the budget applies to the provided date.
     *
     * @param date {LocalDate} The date to check the budget against.
     * @returns True if the budget applies to the date; false otherwise.
     */
    fun isApplicableToDate(date: OffsetDateTime): Boolean {
        val matchesExactDate = month == date.monthValue && year == date.year
        val matchesYear = month == date.monthValue && interval == Interval.Y
        val matchesMonth = (date.monthValue > month || date.year > year) && interval == Interval.M

        return matchesExactDate || matchesYear || matchesMonth
    }

    /**
     * Returns a copy of the budget for the date provided.
     *
     * @param date {LocalDate} The date to update the budget with.
     * @returns A copy of the existing budget with the new date.
     */
    fun copyForDate(date: OffsetDateTime): Budget? {
        return Budget(
            date.monthValue,
            date.year,
            interval,
            amount,
            currency,
            dateLastUpdated
        )
    }
}
