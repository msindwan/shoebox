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

import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit


data class OffsetDateTimeRange(
    val startDate: OffsetDateTime?,
    val endDate: OffsetDateTime?
) {
    val years: Long
        get() = ChronoUnit.YEARS.between(startDate, endDate?.plusDays(1))

    val months: Long
        get() = ChronoUnit.MONTHS.between(startDate, endDate?.plusDays(1))

    companion object {
        /**
         * Returns the current month.
         *
         * @returns the current month as a local date range.
         */
        fun currentMonth(zoneId: ZoneId = ZoneId.systemDefault()): OffsetDateTimeRange {
            val now = OffsetDateTime.now(zoneId)
            val yearMonth = YearMonth.from(now)
            val start = yearMonth.atDay(1).atStartOfDay().atOffset(now.offset)
            val end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atOffset(now.offset)
            return OffsetDateTimeRange(start, end)
        }

        /**
         * Returns the current year.
         *
         * @returns the current year as a local date range.
         */
        fun currentYear(zoneId: ZoneId = ZoneId.systemDefault()): OffsetDateTimeRange {
            val now = YearMonth.from(Instant.now().atZone(zoneId))
            return this.year(now.year, zoneId)
        }

        /**
         * Returns an instant range for the given year.
         *
         * @param year {Int} The year to return a range for.
         * @returns an Instant range from the first moment of the year to the last.
         */
        fun year(year: Int, zoneId: ZoneId = ZoneId.systemDefault()): OffsetDateTimeRange {
            val now = OffsetDateTime.now(zoneId)
            return OffsetDateTimeRange(
                YearMonth.of(year, 1).atDay(1).atTime(LocalTime.MIN).atOffset(now.offset),
                YearMonth.of(year, 12).atEndOfMonth().atTime(LocalTime.MAX).atOffset(now.offset)
            )
        }
    }

    /**
     * Subtracts from the end date.
     *
     * @param amountToRemove {Long} The temporal amount to subtract.
     * @param unit {ChronoUnit} The unit of the amount.
     * @returns A new date range with the updated end date.
     */
    fun minusEnd(amountToRemove: Long, unit: ChronoUnit): OffsetDateTimeRange {
        return OffsetDateTimeRange(
            startDate,
            endDate?.minus(amountToRemove, unit)
        )
    }

    /**
     * Adds to the end date.
     *
     * @param amountToAdd {Long} The temporal amount to add.
     * @param unit {ChronoUnit} The unit of the amount.
     * @returns A new date range with the updated end date.
     */
    fun plusEnd(amountToAdd: Long, unit: ChronoUnit): OffsetDateTimeRange {
        return OffsetDateTimeRange(
            startDate,
            endDate?.plus(amountToAdd, unit)
        )
    }
}
