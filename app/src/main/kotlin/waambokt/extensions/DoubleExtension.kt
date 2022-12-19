package waambokt.extensions

import java.math.RoundingMode

object DoubleExtension {
    fun Double.convertPercent() =
        (this * 100).toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
}
