package com.republicate.kson

actual suspend fun getResource(path: String) = {}.javaClass.classLoader.getResource(path).readText()
