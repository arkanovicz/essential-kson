package com.republicate.kson

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.encodeURLPathPart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

val client = HttpClient()

@OptIn(DelicateCoroutinesApi::class)
suspend fun getResourceImpl(path: String): String {
    // URL-encode path segments to handle special characters like #
    val encodedPath = path.split('/').joinToString("/") { it.encodeURLPathPart() }
    return GlobalScope.async {
        client.get(encodedPath).body<String>()
    }.await()
}
