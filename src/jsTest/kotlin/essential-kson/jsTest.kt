package com.republicate.json

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun testExample() {
        assertTrue(Platform.name.contains("JS"), "Check JS is mentioned")
    }
}
