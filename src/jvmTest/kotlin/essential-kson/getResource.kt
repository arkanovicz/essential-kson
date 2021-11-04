package com.republicate.kson

import kotlinx.io.Input

actual suspend fun getResource(path: String) = {}.javaClass.classLoader.getResource(path).readText()
