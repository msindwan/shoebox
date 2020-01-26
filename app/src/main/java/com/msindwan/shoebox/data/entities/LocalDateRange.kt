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

import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.YearMonth


data class LocalDateRange(
    val startDate: LocalDate?,
    val endDate: LocalDate?
) {
    val period: Period
        get() = Period.between(startDate, endDate)

    companion object {
        /**
         * Returns the current month.
         *
         * @returns the current month as a local date range.
         */
        fun currentMonth(): LocalDateRange {
            val start = YearMonth.now().atDay(1)
            val end = YearMonth.now().atEndOfMonth()
            return LocalDateRange(start, end)
        }

        /**
         * Returns the current year.
         *
         * @returns the current year as a local date range.
         */
        fun currentYear(): LocalDateRange {
            val year = YearMonth.now().year
            return LocalDateRange(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 8, 1)
            )
        }
    }
}
