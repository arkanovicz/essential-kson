package com.republicate.kson


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.promise
import kotlinx.coroutines.GlobalScope


val client = HttpClient()

actual suspend fun getResource(path: String) = getResourceImpl(path)


@OptIn(DelicateCoroutinesApi::class)
suspend fun getResourceImpl(path: String): String {
    return GlobalScope.async {
        client.get(path).body<String>()
    }.await()
}

actual object Platform {
    actual fun js() = true
    actual fun jvm() = false
    actual fun native() = false
    actual fun wasm() = false
}
