package com.republicate.kson

// Check if running in Node.js
private fun isNodeJs(): Boolean = js("typeof process !== 'undefined' && process.versions != null && process.versions.node != null") as? Boolean ?: false

// Read file using Node.js fs module (dynamically loaded to avoid bundling in browser)
private fun readFileNode(filePath: String): String {
    return js("require('fs').readFileSync(filePath, 'utf8')") as String
}

// KGP runs node tests from build/js/packages/<name> and copies the test resources to build/processedResources
private fun resolveResource(path: String): String {
    return js("require('path').resolve(process.cwd(), '../../../processedResources/js/test', path)") as String
}

actual suspend fun getResource(path: String): String {
    return if (isNodeJs()) {
        readFileNode(resolveResource(path))
    } else {
        // Browser: use HTTP client from webTest
        getResourceImpl(path)
    }
}

actual object Platform {
    actual fun js() = true
    actual fun jvm() = false
    actual fun native() = false
    actual fun wasm() = false
}
