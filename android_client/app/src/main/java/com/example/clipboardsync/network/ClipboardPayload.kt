package com.example.clipboardsync.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClipboardPayload(
    val kind: String,
    val mime: String,
    @SerialName("data_base64")
    val dataBase64: String
)
