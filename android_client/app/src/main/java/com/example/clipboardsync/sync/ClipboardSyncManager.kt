package com.example.clipboardsync.sync

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.clipboardsync.data.AppSettings
import com.example.clipboardsync.data.SettingsRepository
import com.example.clipboardsync.network.ClipboardPayload
import java.io.File
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
    private val fileProviderAuthority = "${appContext.packageName}.fileprovider"

    suspend fun pushClipboard(): SyncResult = withContext(Dispatchers.IO) {
        val settings = settingsRepository.settings.first()
        val baseUrl = settings.baseUrl()
            ?: return@withContext SyncResult.Failure("Configure host, port, and token first.")
        val payload = readClipboardPayload()
            ?: return@withContext SyncResult.Failure("Android clipboard does not contain supported text or image data.")

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
                val decoded = decodeFromBase64(payload.dataBase64)
                    ?: return@withContext SyncResult.Failure("Clipboard response was not valid base64 data.")

                when {
                    payload.kind == "text" && payload.mime == "text/plain" -> {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText("Clipboard Sync", String(decoded, Charsets.UTF_8))
                        )
                        return@withContext SyncResult.Success("Clipboard pulled from laptop.")
                    }

                    payload.kind == "image" && payload.mime.startsWith("image/") -> {
                        val clipData = createImageClipData(decoded, payload.mime)
                            ?: return@withContext SyncResult.Failure("Could not place pulled image into Android clipboard.")
                        clipboardManager.setPrimaryClip(clipData)
                        return@withContext SyncResult.Success("Image pulled from laptop.")
                    }

                    else -> {
                        return@withContext SyncResult.Failure("Server clipboard kind or MIME type is not supported.")
                    }
                }
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

    private fun readClipboardPayload(): ClipboardPayload? {
        val clip = clipboardManager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        val description = clipboardManager.primaryClipDescription ?: return null
        val item = clip.getItemAt(0)

        val isText =
            description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        if (isText) {
            val text = item.text?.toString()?.takeIf { it.isNotBlank() } ?: return null
            return ClipboardPayload(
                kind = "text",
                mime = "text/plain",
                dataBase64 = encodeToBase64(text.toByteArray(Charsets.UTF_8))
            )
        }

        val uri = item.uri ?: return null
        val mime = appContext.contentResolver.getType(uri)?.takeIf { it.startsWith("image/") } ?: return null
        val bytes = readUriBytes(uri) ?: return null
        return ClipboardPayload(
            kind = "image",
            mime = mime,
            dataBase64 = encodeToBase64(bytes)
        )
    }

    private fun readUriBytes(uri: Uri): ByteArray? {
        return try {
            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun createImageClipData(bytes: ByteArray, mime: String): ClipData? {
        val extension = fileExtensionForMime(mime)
        val imageFile = File(appContext.cacheDir, "clipboard_pull.$extension")
        return try {
            imageFile.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(appContext, fileProviderAuthority, imageFile)
            ClipData.newUri(appContext.contentResolver, "Clipboard Sync Image", uri)
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun fileExtensionForMime(mime: String): String {
        return when (mime.lowercase()) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            "image/bmp" -> "bmp"
            else -> "img"
        }
    }

    private fun encodeToBase64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decodeFromBase64(data: String): ByteArray? {
        return try {
            Base64.decode(data, Base64.DEFAULT)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

sealed interface SyncResult {
    data class Success(val message: String) : SyncResult
    data class Failure(val message: String) : SyncResult
}
