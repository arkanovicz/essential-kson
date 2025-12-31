package com.republicate.kson

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

val client = HttpClient()

@OptIn(DelicateCoroutinesApi::class)
suspend fun getResourceImpl(path: String): String {
    return GlobalScope.async {
        client.get(path).body<String>()
    }.await()
}
