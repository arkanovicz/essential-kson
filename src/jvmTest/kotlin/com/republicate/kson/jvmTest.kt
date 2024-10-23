package com.republicate.kson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual suspend fun getResource(path: String) = {}.javaClass.classLoader.getResource(path).readText()

actual object Platform {
    actual fun js() = false
    actual fun jvm() = true
    actual fun native() = false
    actual fun wasm() = false
}
