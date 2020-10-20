package com.republicate.json

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlin.js.Promise

val client = HttpClient()

actual suspend fun getResource(path: String) : String {

    var content = GlobalScope.async {
        client.get<String> { url(path) }
    }
    return content.await()
}
