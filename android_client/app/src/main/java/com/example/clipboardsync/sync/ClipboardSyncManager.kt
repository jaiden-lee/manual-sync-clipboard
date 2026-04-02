package com.example.clipboardsync.sync

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import com.example.clipboardsync.data.AppSettings
import com.example.clipboardsync.data.SettingsRepository
import com.example.clipboardsync.network.ClipboardPayload
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ClipboardSyncManager(
    context: Context,
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val appContext = context.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val clipboardManager =
        appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    suspend fun pushClipboard(): SyncResult = withContext(Dispatchers.IO) {
        val settings = settingsRepository.settings.first()
        val baseUrl = settings.baseUrl()
            ?: return@withContext SyncResult.Failure("Configure host, port, and token first.")
        val text = readClipboardText()?.takeIf { it.isNotBlank() }
            ?: return@withContext SyncResult.Failure("Android clipboard does not contain plain text.")

        val payload = ClipboardPayload(
            kind = "text",
            mime = "text/plain",
            dataBase64 = encodeToBase64(text)
        )
        val request = Request.Builder()
            .url("$baseUrl/clipboard")
            .header("Authorization", "Bearer ${settings.token.trim()}")
            .post(
                json.encodeToString(ClipboardPayload.serializer(), payload)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        executeWithoutBody(request, "Clipboard pushed to laptop.")
    }

    suspend fun pullClipboard(): SyncResult = withContext(Dispatchers.IO) {
        val settings = settingsRepository.settings.first()
        val baseUrl = settings.baseUrl()
            ?: return@withContext SyncResult.Failure("Configure host, port, and token first.")
        val request = Request.Builder()
            .url("$baseUrl/clipboard")
            .header("Authorization", "Bearer ${settings.token.trim()}")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SyncResult.Failure("Pull failed with HTTP ${response.code}.")
                }

                val body = response.body?.string().orEmpty()
                val payload = json.decodeFromString(ClipboardPayload.serializer(), body)
                if (payload.kind != "text" || payload.mime != "text/plain") {
                    return@withContext SyncResult.Failure("Server clipboard is not plain text.")
                }

                val decoded = decodeFromBase64(payload.dataBase64)
                    ?: return@withContext SyncResult.Failure("Clipboard response was not valid base64 text.")

                clipboardManager.setPrimaryClip(ClipData.newPlainText("Clipboard Sync", decoded))
                return@withContext SyncResult.Success("Clipboard pulled from laptop.")
            }
        } catch (error: IOException) {
            SyncResult.Failure(error.message ?: "Could not reach the laptop server.")
        } catch (_: SerializationException) {
            SyncResult.Failure("Laptop response was not valid clipboard JSON.")
        }
    }

    suspend fun loadSettings(): AppSettings = settingsRepository.settings.first()

    suspend fun saveSettings(settings: AppSettings) {
        settingsRepository.save(settings)
    }

    private fun executeWithoutBody(request: Request, successMessage: String): SyncResult {
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    SyncResult.Success(successMessage)
                } else {
                    SyncResult.Failure("Request failed with HTTP ${response.code}.")
                }
            }
        } catch (error: IOException) {
            SyncResult.Failure(error.message ?: "Could not reach the laptop server.")
        }
    }

    private fun readClipboardText(): String? {
        val clip = clipboardManager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        val description = clipboardManager.primaryClipDescription ?: return null
        val isText =
            description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        if (!isText) return null

        return clip.getItemAt(0).text?.toString()
    }

    private fun encodeToBase64(text: String): String =
        Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    private fun decodeFromBase64(data: String): String? {
        return try {
            String(Base64.decode(data, Base64.DEFAULT), Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

sealed interface SyncResult {
    data class Success(val message: String) : SyncResult
    data class Failure(val message: String) : SyncResult
}
