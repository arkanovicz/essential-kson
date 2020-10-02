package com.republicate.json

import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformTest {

    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", Platform.name.contains("Android"))
    }
}
