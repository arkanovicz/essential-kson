package com.republicate.kson

import kotlin.time.Clock
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

fun Double.toString(numOfDec: Int): String {
    val integerDigits = this.toInt()
    val floatDigits = ((this - integerDigits) * 10f.pow(numOfDec)).roundToInt()
    return "${integerDigits}.${floatDigits}"
}

expect suspend fun getResource(path: String): String

@OptIn(ExperimentalTime::class)
abstract class BaseTestUnit
{
    var startNanos = 0L
    var elapsedMillis = 0.0

    fun checksum(str: String) : Int {
        var ret = 0
        var insideString = false
        var escaped = false
        for (c in str.toCharArray()) {
            if (insideString) {
                if (escaped) escaped = false
                else if (c == '"') insideString = false
            }
            else if (c == '"') insideString = true
            if (insideString || !c.isWhitespace()) {
                ret = ret xor c.code
            }
        }
        return ret
    }

    fun startTiming() {
        startNanos = Clock.System.now().toEpochMilliseconds()
    }

    fun stopTiming() {
        elapsedMillis = Clock.System.now().toEpochMilliseconds() - startNanos / 1000000.0
    }

    fun elapsed() = "${elapsedMillis.toString(2)}ms"
}
