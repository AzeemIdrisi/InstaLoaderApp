package com.alphacorp.instaloader.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedSessionStore(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    val sessionDir: String?
        get() = prefs.getString(KEY_SESSION_DIR, null)

    fun initializeSessionDir(path: String) {
        prefs.edit().putString(KEY_SESSION_DIR, path).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "instaloader_session"
        private const val KEY_USERNAME = "username"
        private const val KEY_SESSION_DIR = "session_dir"
    }
}
