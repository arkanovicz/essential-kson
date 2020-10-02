package com.republicate.json

actual object Platform {
    actual val name = "Android ${android.os.Build.VERSION.SDK_INT}"
}
