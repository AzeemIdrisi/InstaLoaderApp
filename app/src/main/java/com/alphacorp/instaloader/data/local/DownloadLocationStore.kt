package com.alphacorp.instaloader.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.downloadLocationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "download_location",
)

class DownloadLocationStore(private val context: Context) {

    val customPath: Flow<String?> = context.downloadLocationDataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_PATH]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveCustomPath(path: String?) {
        context.downloadLocationDataStore.edit { prefs ->
            if (path.isNullOrBlank()) {
                prefs.remove(Keys.CUSTOM_PATH)
            } else {
                prefs[Keys.CUSTOM_PATH] = path
            }
        }
    }

    suspend fun clear() {
        saveCustomPath(null)
    }

    private object Keys {
        val CUSTOM_PATH = stringPreferencesKey("custom_download_path")
    }
}
