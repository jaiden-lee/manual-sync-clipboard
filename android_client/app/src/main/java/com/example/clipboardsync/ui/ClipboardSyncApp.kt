package com.example.clipboardsync.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPasteGo
import androidx.compose.material.icons.rounded.Publish
import androidx.compose.material.icons.rounded.SettingsEthernet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clipboardsync.ui.theme.BorderSoft
import com.example.clipboardsync.ui.theme.ErrorRed
import com.example.clipboardsync.ui.theme.InkBlue
import com.example.clipboardsync.ui.theme.MutedText
import com.example.clipboardsync.ui.theme.SuccessGreen
import com.example.clipboardsync.ui.theme.TealAccent

@Composable
fun ClipboardSyncApp(viewModel: ClipboardSyncViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var contentVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFFE9F2F5),
                            Color(0xFFDCEBF0)
                        ),
                        start = Offset.Zero,
                        end = Offset(1200f, 1800f)
                    )
                )
                .padding(innerPadding)
        ) {
            val alpha by animateFloatAsState(
                targetValue = if (contentVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                label = "content-alpha"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                HeroSection(endpointPreview = uiState.endpointPreview)
                SettingsCard(
                    uiState = uiState,
                    onHostChanged = viewModel::onHostChanged,
                    onPortChanged = viewModel::onPortChanged,
                    onTokenChanged = viewModel::onTokenChanged,
                    onSave = viewModel::saveSettings,
                    onTestPush = viewModel::testPush,
                    onTestPull = viewModel::testPull
                )
                InfoCard()
            }
        }
    }
}

@Composable
private fun HeroSection(endpointPreview: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(InkBlue, TealAccent),
                        start = Offset.Zero,
                        end = Offset(1000f, 700f)
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SettingsEthernet,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Text(
                    text = "Clipboard bridge",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            Text(
                text = "Connect your phone to the laptop relay once, then trigger PUSH and PULL from Quick Settings.",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            Text(
                text = endpointPreview,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun SettingsCard(
    uiState: ClipboardSyncUiState,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onTokenChanged: (String) -> Unit,
    onSave: () -> Unit,
    onTestPush: () -> Unit,
    onTestPull: () -> Unit
) {
    var revealToken by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Connection settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "The laptop hosts the clipboard endpoint. The phone only needs its address and matching token.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )
            }

            OutlinedTextField(
                value = uiState.host,
                onValueChange = onHostChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Laptop IP or host") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            OutlinedTextField(
                value = uiState.port,
                onValueChange = onPortChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Port") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(18.dp)
            )

            OutlinedTextField(
                value = uiState.token,
                onValueChange = onTokenChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Shared token") },
                singleLine = true,
                visualTransformation = if (revealToken) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(onClick = { revealToken = !revealToken }) {
                        Text(if (revealToken) "Hide" else "Show")
                    }
                },
                shape = RoundedCornerShape(18.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF6FAFB)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Endpoint preview",
                        style = MaterialTheme.typography.labelLarge,
                        color = MutedText
                    )
                    Text(
                        text = uiState.endpointPreview,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlue
                    )
                    Text(
                        text = "Requests use Authorization: Bearer <token>.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
            }

            AnimatedVisibility(visible = uiState.statusMessage.isNotBlank()) {
                StatusBanner(
                    text = uiState.statusMessage,
                    isError = uiState.isError
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !uiState.isWorking,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Save Settings")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onTestPush,
                        enabled = !uiState.isWorking,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Publish, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Test Push")
                    }
                    OutlinedButton(
                        onClick = onTestPull,
                        enabled = !uiState.isWorking,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.ContentPasteGo, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Test Pull")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(text: String, isError: Boolean) {
    val borderColor = if (isError) ErrorRed.copy(alpha = 0.45f) else SuccessGreen.copy(alpha = 0.35f)
    val contentColor = if (isError) ErrorRed else SuccessGreen

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = contentColor.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "How the tiles behave",
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(color = BorderSoft)
            Text(
                text = "PUSH sends the phone's latest plain-text clipboard item to the laptop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
            Text(
                text = "PULL reads the laptop's latest plain-text clipboard item and writes it into Android clipboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
            Text(
                text = "If the phone clipboard is not text, PUSH safely does nothing and reports failure.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }
    }
}
