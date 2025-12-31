package com.republicate.kson

actual suspend fun getResource(path: String) = getResourceImpl(path)

actual object Platform {
    actual fun js() = true
    actual fun jvm() = false
    actual fun native() = false
    actual fun wasm() = false
}
