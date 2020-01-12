package com.msindwan.shoebox.helpers

import java.nio.ByteBuffer
import java.util.*

object UUIDHelpers {
    fun getBytesFromUUID(uuid: UUID): ByteArray {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)

        return bb.array()
    }
}