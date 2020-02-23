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
package com.msindwan.shoebox.helpers

import java.util.*


/**
 * Utility methods for formatting numbers.
 */
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

    /**
     * Returns a formatted compact version of the number.
     * Implementation from https://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java
     *
     * @param value {Long} The value to format.
     * @returns the compact version (e.g 1.2K)
     */
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