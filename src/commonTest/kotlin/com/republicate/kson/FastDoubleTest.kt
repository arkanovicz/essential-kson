package com.republicate.kson

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class FastDoubleTest {

    private fun check(negative: Boolean, w: Long, q: Int) {
        val fast = FastDouble.parse(negative, w, q)
        if (fast.isNaN()) return // fallback path, exercised via Json.parse below
        val literal = "${if (negative) "-" else ""}${w}e$q"
        val expected = literal.toDouble()
        assertEquals(expected.toRawBits(), fast.toRawBits(), "mismatch for $literal: expected $expected, got $fast")
    }

    @Test
    fun againstPlatformParser() {
        val rnd = Random(42)
        repeat(50_000) {
            val digits = rnd.nextInt(1, 16)
            var w = 0L
            repeat(digits) { w = w * 10 + rnd.nextInt(10) }
            check(rnd.nextBoolean(), w, rnd.nextInt(-330, 309))
        }
    }

    @Test
    fun exponentBoundaries() {
        for (q in intArrayOf(-330, -323, -322, -308, -23, -22, -1, 0, 1, 22, 23, 308)) {
            for (w in longArrayOf(1, 5, 9, 17, 999, 1000000, 999999999999999L)) {
                check(false, w, q)
                check(true, w, q)
            }
        }
    }

    @Test
    fun throughParser() {
        // literal to expected double, covering fast path, fallback and sign handling
        val cases = mapOf(
            "3.14" to 3.14,
            "-3.14" to -3.14,
            "0.001" to 0.001,
            "1.5e10" to 1.5e10,
            "-1.5e-10" to -1.5e-10,
            "0.00000000000001e-307" to 1e-321, // subnormal, falls back to the platform parser
            "1e307" to 1e307,
            "123.456e21" to 123.456e21,
            "99999999999999.9" to 99999999999999.9,
            "7.20575940379279e16" to 7.20575940379279e16
        )
        for ((literal, expected) in cases) {
            val parsed = Json.parseValue(literal) as Double
            assertEquals(expected.toRawBits(), parsed.toRawBits(), "Json.parseValue(\"$literal\")")
        }
        // -0.0 keeps its sign
        assertEquals((-0.0).toRawBits(), (Json.parseValue("-0.0") as Double).toRawBits())
    }
}
