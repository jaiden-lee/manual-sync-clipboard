package com.example.clipboardsync.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "clipboard_sync_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val host = stringPreferencesKey("host")
        val port = stringPreferencesKey("port")
        val token = stringPreferencesKey("token")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            host = preferences[Keys.host].orEmpty(),
            port = preferences[Keys.port].orEmpty(),
            token = preferences[Keys.token].orEmpty()
        )
    }

    suspend fun save(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[Keys.host] = settings.host.trim()
            preferences[Keys.port] = settings.port.trim()
            preferences[Keys.token] = settings.token.trim()
        }
    }
}
