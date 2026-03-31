package com.example.clipboardsync.tile

import com.example.clipboardsync.sync.ClipboardSyncManager
import com.example.clipboardsync.sync.SyncResult

class PushTileService : BaseClipboardTileService() {
    override val idleLabel: String = "PUSH"
    override val workingLabel: String = "PUSHING"

    override suspend fun performAction(syncManager: ClipboardSyncManager): SyncResult {
        return syncManager.pushClipboard()
    }
}
