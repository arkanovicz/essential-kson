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

/*
actual fun runTest(body: suspend CoroutineScope.() -> Unit) = runBlocking<Unit> {
    body()
}
 */

/*
actual fun runTest(body: suspend CoroutineScope.() -> Unit) {
    runBlocking<Unit> {
        body()
    }
}
 */

/*
fun runTestImpl(body: suspend CoroutineScope.() -> Unit) =
    GlobalScope.promise { body() }
 */

@OptIn(DelicateCoroutinesApi::class)
actual fun runTest(body: suspend CoroutineScope.() -> Unit) {
    val job = GlobalScope.launch {
        body()
    }
    /*
    while (job.isActive) {
        println("JOB ACTIVE")
    }
     */
}

actual object platform {
    actual fun js() = false
    actual fun jvm() = false
    actual fun native() = false
}
