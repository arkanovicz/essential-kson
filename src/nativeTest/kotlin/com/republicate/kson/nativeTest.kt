package com.republicate.kson

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
// import io.github.oshai.kotlinlogging.KotlinLoggingLevel
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen

actual suspend fun getResource(path: String) = getResourceImpl(path)

// see https://www.nequalsonelifestyle.com/2020/11/16/kotlin-native-file-io/
@OptIn(ExperimentalForeignApi::class)
fun getResourceImpl(path: String): String {
    val returnBuffer = StringBuilder()
    // for now, fetch resources from src dirs - TODO
    val file = fopen("src/commonTest/resources/$path", "r") ?: throw IllegalArgumentException("Cannot open input file $path")

    try {
        memScoped {
            val readBufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(readBufferLength)
            var line = fgets(buffer, readBufferLength, file)?.toKString()
            while (line != null) {
                returnBuffer.append(line)
                line = fgets(buffer, readBufferLength, file)?.toKString()
            }
        }
    } finally {
        fclose(file)
    }

    return returnBuffer.toString()
}

actual object Platform {
    actual fun js() = false
    actual fun jvm() = false
    actual fun native() = true
    actual fun wasm() = false
}
