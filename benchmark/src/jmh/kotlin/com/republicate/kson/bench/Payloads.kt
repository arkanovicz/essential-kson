package com.republicate.kson.bench

/**
 * Deterministic ~4KB payloads, generated from index arithmetic only (no RNG),
 * so every run of every JVM parses the exact same bytes.
 */
object Payloads {

    private const val TARGET = 4096

    val MIXED: String = buildArray { i ->
        val id = 100000 + i * 37
        val score = (i * 7919 % 10000) / 100.0
        val ratio = (i * 104729 % 100000) / 100000.0
        val active = i % 3 != 0
        val note = if (i % 5 == 0) "null" else "\"lorem ipsum dolor sit amet #$i\""
        """{"id":$id,"name":"user_${(i % 1000).toString().padStart(4, '0')}",""" +
        """"score":$score,"active":$active,"note":$note,"ratio":$ratio,""" +
        """"tags":["tag${i % 17}","kind-${i % 4}"],""" +
        """"meta":{"created":"2024-${(i % 12 + 1).toString().padStart(2, '0')}-15T08:${(i % 60).toString().padStart(2, '0')}:00Z","rev":${i % 23}}}"""
    }

    val STRINGS: String = buildArray { i ->
        val filler = "abcdefghijklmnopqrstuvwxyz0123456789".let { it + it }.substring(0, 24 + i % 31)
        when (i % 4) {
            0 -> "\"$filler segment-$i plain ascii text value\""
            1 -> "\"$filler with \\\"quotes\\\" and \\\\backslash\\\\ and \\t tab\""
            2 -> "\"$filler caf\\u00e9 na\\u00efve \\u4e2d\\u6587 \\u00e9\\u00e8\\u00ea\""
            else -> "\"$filler line\\nbreak\\r and slash \\/ end-$i\""
        }
    }

    val NUMBERS: String = buildArray { i ->
        when (i % 6) {
            0 -> "${i - 500}"
            1 -> "${1234567890123L + i * 7919L}"
            2 -> "${(i * 31 % 997) / 7.0}"
            3 -> "${i % 100}.${(i * 13 % 1000).toString().padStart(3, '0')}e${if (i % 2 == 0) "+" else "-"}${i % 12}"
            4 -> "-${i * 3 + 1}"
            else -> "0.${(i * 104729 % 1000000).toString().padStart(6, '0')}"
        }
    }

    /** Accumulates elements into a JSON array until the target size is reached. */
    private fun buildArray(element: (Int) -> String): String {
        val sb = StringBuilder("[")
        var i = 0
        while (sb.length < TARGET) {
            if (i > 0) sb.append(',')
            sb.append(element(i))
            ++i
        }
        return sb.append(']').toString()
    }

    fun sizes() = "mixed=${MIXED.length}B strings=${STRINGS.length}B numbers=${NUMBERS.length}B"
}
