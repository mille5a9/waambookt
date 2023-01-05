package org.waambokt.common.extensions

import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TimestampExtension {
    fun Timestamp.getDays() =
        LocalDate.ofInstant(Instant.ofEpochSecond(this.seconds), ZoneId.of("UTC")).toEpochDay()

    fun Timestamp.getInstant(): Instant =
        Instant.ofEpochSecond(this.seconds)
}
