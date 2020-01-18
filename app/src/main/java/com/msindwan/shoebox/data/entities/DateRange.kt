package com.msindwan.shoebox.data.entities

data class DateRange(val startDate: Long, val endDate: Long) {
    companion object {
        const val NO_START_DATE: Long = -1
        const val NO_END_DATE: Long = -1
    }
}
