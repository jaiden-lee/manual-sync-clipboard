package com.example.clipboardsync.data

data class AppSettings(
    val host: String = "",
    val port: String = "",
    val token: String = ""
) {
    fun isComplete(): Boolean = host.isNotBlank() && port.isNotBlank() && token.isNotBlank()

    fun baseUrl(): String? {
        if (!isComplete()) return null
        val normalizedHost = host.trim().trimEnd('/')
        val normalizedPort = port.trim()
        return "http://$normalizedHost:$normalizedPort"
    }
}
