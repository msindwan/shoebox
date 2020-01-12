package com.msindwan.shoebox.helpers

import com.msindwan.shoebox.data.entities.DateRange
import java.util.*

object DateHelpers {
    fun getCurrentMonth(): DateRange {
        val beginning: Long
        val end: Long

        run {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.set(
                Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
            )
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            beginning = calendar.timeInMillis / 1000
        }

        run {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.set(
                Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            end = calendar.timeInMillis / 1000
        }

        return DateRange(beginning, end)
    }
}