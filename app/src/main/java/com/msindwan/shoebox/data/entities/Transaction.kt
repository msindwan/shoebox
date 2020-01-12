package com.msindwan.shoebox.data.entities

data class Transaction(
    val id: ByteArray,
    val date: Long,
    val title: String?,
    val amount: Long,
    val type: String,
    val date_created: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (!id.contentEquals(other.id)) return false

        return true
    }

    override fun hashCode(): Int {
        return id.contentHashCode()
    }
}
