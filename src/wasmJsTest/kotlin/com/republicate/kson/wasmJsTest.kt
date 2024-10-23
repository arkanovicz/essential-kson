package com.republicate.kson

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual suspend fun getResource(path: String) = getResourceImpl(path)

fun getResourceImpl(path: String): String {
    val returnBuffer = StringBuilder()
    returnBuffer.append("WIP")
    return returnBuffer.toString()
}

actual object Platform {
    actual fun js() = false
    actual fun jvm() = false
    actual fun native() = false
    actual fun wasm() = true
}
