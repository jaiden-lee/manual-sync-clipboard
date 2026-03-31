package com.example.clipboardsync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clipboardsync.data.AppSettings
import com.example.clipboardsync.sync.ClipboardSyncManager
import com.example.clipboardsync.sync.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClipboardSyncUiState(
    val host: String = "",
    val port: String = "",
    val token: String = "",
    val isWorking: Boolean = false,
    val statusMessage: String = "Add your laptop connection details, then use the tiles or test buttons.",
    val isError: Boolean = false
) {
    val endpointPreview: String
        get() = if (host.isBlank() || port.isBlank()) {
            "http://host:port/clipboard"
        } else {
            "http://${host.trim()}:${port.trim()}/clipboard"
        }
}

class ClipboardSyncViewModel(application: Application) : AndroidViewModel(application) {
    private val syncManager = ClipboardSyncManager(application)
    private val _uiState = MutableStateFlow(ClipboardSyncUiState())
    val uiState: StateFlow<ClipboardSyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = syncManager.loadSettings()
            _uiState.update {
                it.copy(
                    host = settings.host,
                    port = settings.port,
                    token = settings.token
                )
            }
        }
    }

    fun onHostChanged(value: String) {
        _uiState.update { it.copy(host = value) }
    }

    fun onPortChanged(value: String) {
        _uiState.update { it.copy(port = value.filter(Char::isDigit)) }
    }

    fun onTokenChanged(value: String) {
        _uiState.update { it.copy(token = value) }
    }

    fun saveSettings() {
        val settings = currentSettings() ?: run {
            showError("Enter a valid host, numeric port, and shared token.")
            return
        }

        performAction("Saving connection settings...") {
            syncManager.saveSettings(settings)
            SyncResult.Success("Settings saved. Quick Settings tiles can now use this connection.")
        }
    }

    fun testPush() {
        if (currentSettings() == null) {
            showError("Save a valid host, port, and token before testing.")
            return
        }

        performAction("Pushing phone clipboard...") {
            syncManager.pushClipboard()
        }
    }

    fun testPull() {
        if (currentSettings() == null) {
            showError("Save a valid host, port, and token before testing.")
            return
        }

        performAction("Pulling laptop clipboard...") {
            syncManager.pullClipboard()
        }
    }

    private fun performAction(progressMessage: String, action: suspend () -> SyncResult) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isWorking = true,
                    statusMessage = progressMessage,
                    isError = false
                )
            }

            when (val result = action()) {
                is SyncResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            statusMessage = result.message,
                            isError = false
                        )
                    }
                }

                is SyncResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            statusMessage = result.message,
                            isError = true
                        )
                    }
                }
            }
        }
    }

    private fun currentSettings(): AppSettings? {
        val state = _uiState.value
        val normalizedPort = state.port.trim()
        val portNumber = normalizedPort.toIntOrNull()
        if (state.host.isBlank() || state.token.isBlank() || portNumber == null || portNumber !in 1..65535) {
            return null
        }

        return AppSettings(
            host = state.host.trim(),
            port = normalizedPort,
            token = state.token.trim()
        )
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isWorking = false,
                statusMessage = message,
                isError = true
            )
        }
    }
}
