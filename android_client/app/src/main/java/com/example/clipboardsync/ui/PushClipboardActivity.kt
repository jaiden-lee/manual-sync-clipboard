package com.example.clipboardsync.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.clipboardsync.sync.ClipboardSyncManager
import com.example.clipboardsync.ui.theme.ClipboardSyncTheme
import com.example.clipboardsync.ui.theme.InkBlue
import com.example.clipboardsync.ui.theme.TealAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PushClipboardActivity : ComponentActivity() {
    private val syncManager by lazy { ClipboardSyncManager(applicationContext) }
    private var uiState by mutableStateOf(PushOverlayUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setFinishOnTouchOutside(false)
        setContent {
            ClipboardSyncTheme {
                PushOverlayScreen(
                    uiState = uiState,
                    onConfirm = ::runPushIfReady
                )
            }
        }

        uiState = uiState.copy(isVisible = true)
    }

    private fun runPushIfReady() {
        if (uiState.isWorking || uiState.hasCompleted) return
        lifecycleScope.launch {
            uiState = uiState.copy(
                title = "Uploading",
                message = "Reading clipboard and sending it to your laptop.",
                isWorking = true,
                hasCompleted = true,
                isVisible = true
            )
            syncManager.pushClipboard()
            delay(250)
            uiState = uiState.copy(isVisible = false)
            delay(120)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}

private data class PushOverlayUiState(
    val title: String = "Tap to push",
    val message: String = "Tap anywhere to let Clipboard Sync read your clipboard and upload it.",
    val accent: Color = TealAccent,
    val isWorking: Boolean = false,
    val isVisible: Boolean = false,
    val hasCompleted: Boolean = false
)

@Composable
private fun PushOverlayScreen(
    uiState: PushOverlayUiState,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.14f))
            .clickable(enabled = !uiState.isWorking && !uiState.hasCompleted, onClick = onConfirm)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = uiState.isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(InkBlue, uiState.accent),
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isWorking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    if (!uiState.isWorking && !uiState.hasCompleted) {
                        Text(
                            text = "Tap anywhere",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
