package com.republicate.json

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.GlobalScope


val client = HttpClient()

actual suspend fun getResource(path: String) : String {
    var content = GlobalScope.async {
        client.get<String> { url(path) }
    }
    return content.await()
}
