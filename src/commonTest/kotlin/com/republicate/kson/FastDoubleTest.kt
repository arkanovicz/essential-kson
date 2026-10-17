package com.republicate.kson

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FastDoubleTest {

    private fun check(negative: Boolean, w: Long, q: Int) {
        val fast = FastDouble.parse(negative, w, q)
        if (fast.isNaN()) return // fallback path, exercised via Json.parse below
        val literal = "${if (negative) "-" else ""}${w}e$q"
        val platform = literal.toDouble()
        if (platform.toRawBits() == fast.toRawBits()) return
        // The platform parser is the cheap oracle, but Kotlin/Wasm String.toDouble() (2.3 to 2.4) is not correctly
        // rounded (~2% of random literals, KT-88414, fixed in Kotlin 2.5.0): on disagreement, exact arithmetic decides.
        // TODO Kotlin 2.5.0: drop the exact check and assert platform == fast on every target.
        assertTrue((fast < 0) == negative && isNearest(w, q, abs(fast)), "mismatch for $literal: platform $platform, fast path $fast, not the nearest double")
    }

    // Exact rationals as integers over a common denominator 2^1100 * 10^330, covering subnormals and q >= -330.
    private fun scaled(w: Long, q: Int): BigInteger = BigInteger.fromLong(w) * BigInteger.TEN.pow(q + 330) * BigInteger.TWO.pow(1100)

    private fun scaled(d: Double): BigInteger {
        val bits = d.toRawBits()
        val biasedExp = ((bits shr 52) and 0x7FF).toInt()
        val fraction = bits and 0xFFFFFFFFFFFFFL
        val m = if (biasedExp == 0) fraction else fraction or (1L shl 52)
        val e = if (biasedExp == 0) -1074 else biasedExp - 1075
        return BigInteger.fromLong(m) * BigInteger.TWO.pow(e + 1100) * BigInteger.TEN.pow(330)
    }

    // d is correctly rounded iff no neighbor is closer to the exact value, ties going to the even mantissa
    private fun isNearest(w: Long, q: Int, d: Double): Boolean {
        val exact = scaled(w, q)
        val distance = (scaled(d) - exact).abs()
        for (neighbor in listOf(Double.fromBits(d.toRawBits() - 1), Double.fromBits(d.toRawBits() + 1))) {
            if (!neighbor.isFinite() || neighbor < 0.0) continue
            val neighborDistance = (scaled(neighbor) - exact).abs()
            if (neighborDistance < distance || neighborDistance == distance && (d.toRawBits() and 1L) != 0L) return false
        }
        return true
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
    fun exactOracle() {
        // 9165901164563e-64 sits 0.4958 ulp above this double: Kotlin/Wasm 2.3 picks the next one
        val nearest = Double.fromBits(3843244687683487402L)
        assertTrue(isNearest(9165901164563L, -64, nearest))
        assertTrue(!isNearest(9165901164563L, -64, Double.fromBits(nearest.toRawBits() + 1)))
        assertTrue(!isNearest(9165901164563L, -64, Double.fromBits(nearest.toRawBits() - 1)))
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
