package com.republicate.kson

actual suspend fun getResource(path: String): String {
    // WASI filesystem access is still experimental
    // For now, return a stub
    return "WIP"
}

actual object Platform {
    actual fun js() = false
    actual fun jvm() = false
    actual fun native() = false
    actual fun wasm() = true
}
