package com.msindwan.shoebox.data.entities

data class Budget(
    var startDate: Long,
    var endDate: Long?,
    var amount: Long,
    var currency: String,
    var interval: String,
    var dateLastUpdated: Int
) {
    companion object {
        const val NO_END_DATE: Long = -1
    }
}
