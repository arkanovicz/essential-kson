package com.republicate.json

import kotlinx.io.Input

actual suspend fun getResource(path: String) = {}.javaClass.classLoader.getResource(path).readText()
