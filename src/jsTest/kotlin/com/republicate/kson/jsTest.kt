package com.republicate.kson

// Check if running in Node.js
private fun isNodeJs(): Boolean = js("typeof process !== 'undefined' && process.versions != null && process.versions.node != null") as? Boolean ?: false

// Read file using Node.js fs module (dynamically loaded to avoid bundling in browser)
private fun readFileNode(filePath: String): String {
    return js("require('fs').readFileSync(filePath, 'utf8')") as String
}

// Get project root from __dirname (build output directory)
// __dirname is like /project/build/compileSync/js/test/testDevelopmentExecutable/kotlin
// Need to go 5 levels up: kotlin -> testDevelopmentExecutable -> test -> js -> compileSync -> build -> project
private fun getProjectRoot(): String {
    return js("require('path').resolve(__dirname, '../../../../..')") as String
}

// Resolve path using Node.js path module
private fun resolvePath(vararg paths: String): String {
    return js("require('path').resolve.apply(null, paths)") as String
}

actual suspend fun getResource(path: String): String {
    return if (isNodeJs()) {
        // Node.js: read from filesystem with absolute path
        val projectRoot = getProjectRoot()
        val fullPath = resolvePath(projectRoot, "src/commonTest/resources", path)
        readFileNode(fullPath)
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
