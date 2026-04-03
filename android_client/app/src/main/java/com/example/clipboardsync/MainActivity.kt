package com.example.clipboardsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.clipboardsync.ui.ClipboardSyncApp
import com.example.clipboardsync.ui.theme.ClipboardSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClipboardSyncTheme {
                ClipboardSyncApp()
            }
        }
    }
}