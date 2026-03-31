package com.example.clipboardsync.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.clipboardsync.sync.ClipboardSyncManager
import com.example.clipboardsync.sync.SyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class BaseClipboardTileService : TileService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var syncManager: ClipboardSyncManager

    protected abstract val idleLabel: String
    protected abstract val workingLabel: String
    protected abstract suspend fun performAction(syncManager: ClipboardSyncManager): SyncResult

    override fun onCreate() {
        super.onCreate()
        syncManager = ClipboardSyncManager(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile(idleLabel, null, Tile.STATE_ACTIVE)
    }

    override fun onClick() {
        super.onClick()
        updateTile(workingLabel, "Running", Tile.STATE_UNAVAILABLE)
        serviceScope.launch {
            val result = performAction(syncManager)
            when (result) {
                is SyncResult.Success -> updateTile(idleLabel, "Done", Tile.STATE_ACTIVE)
                is SyncResult.Failure -> updateTile(idleLabel, "Failed", Tile.STATE_INACTIVE)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun updateTile(label: String, subtitle: String?, state: Int) {
        qsTile?.apply {
            this.label = label
            this.subtitle = subtitle
            this.state = state
            updateTile()
        }
    }
}
