package com.republicate.kson

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

actual suspend fun getResource(path: String): String {
    val filePath = Path("src/commonTest/resources/$path")
    return SystemFileSystem.source(filePath).buffered().readString()
}

actual object Platform {
    actual fun js() = false
    actual fun jvm() = false
    actual fun native() = false
    actual fun wasm() = true
}
