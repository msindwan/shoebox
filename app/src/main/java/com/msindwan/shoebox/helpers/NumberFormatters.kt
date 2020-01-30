package com.msindwan.shoebox.helpers

import java.util.*

object NumberFormatters {
    private val suffixes = TreeMap<Long, String>()

    init {
        suffixes[1_000L] = "k"
        suffixes[1_000_000L] = "M"
        suffixes[1_000_000_000L] = "G"
        suffixes[1_000_000_000_000L] = "T"
        suffixes[1_000_000_000_000_000L] = "P"
        suffixes[1_000_000_000_000_000_000L] = "E"
    }

    fun getCompactNumberInstance(value: Long): String {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return getCompactNumberInstance(java.lang.Long.MIN_VALUE + 1)
        if (value < 0) return "-" + getCompactNumberInstance(-value)
        if (value < 1000) return value.toString()

        val e = suffixes.floorEntry(value)
        val divideBy = e?.key ?: 1
        val suffix = e?.value ?: ""

        val truncated = value / (divideBy / 10) //the number part of the output times 10
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }
}