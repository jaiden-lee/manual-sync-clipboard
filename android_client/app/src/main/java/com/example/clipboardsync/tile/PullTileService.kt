package com.example.clipboardsync.tile

import com.example.clipboardsync.sync.ClipboardSyncManager
import com.example.clipboardsync.sync.SyncResult

class PullTileService : BaseClipboardTileService() {
    override val idleLabel: String = "PULL"
    override val workingLabel: String = "PULLING"

    override suspend fun performAction(syncManager: ClipboardSyncManager): SyncResult {
        return syncManager.pullClipboard()
    }
}
