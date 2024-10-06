package com.republicate.kson


import io.ktor.client.HttpClient
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.promise
import kotlinx.coroutines.GlobalScope


val client = HttpClient()

actual suspend fun getResource(path: String) = getResourceSImpl(path)


@OptIn(DelicateCoroutinesApi::class)
suspend fun getResourceSImpl(path: String): String {
    val content = GlobalScope.async {
        client.get<String> { url(path) }
    }
    return content.await()
}

actual fun runTest(body: suspend CoroutineScope.() -> Unit) {
    runTestImpl(body)
}

@OptIn(DelicateCoroutinesApi::class)
fun runTestImpl(body: suspend CoroutineScope.() -> Unit): dynamic =
    GlobalScope.promise { body() }

actual object platform {
    actual fun js() = true
    actual fun jvm() = false
    actual fun native() = false
}
