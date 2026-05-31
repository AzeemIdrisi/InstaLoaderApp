package com.alphacorp.instaloader.data.repository

import android.content.Context
import com.alphacorp.instaloader.data.local.EncryptedSessionStore
import com.alphacorp.instaloader.data.model.DownloadError
import com.alphacorp.instaloader.data.model.toDownloadError
import com.alphacorp.instaloader.data.python.PythonBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SessionRepository(
    private val context: Context,
    private val pythonBridge: PythonBridge,
    private val sessionStore: EncryptedSessionStore,
) {
    val username: String?
        get() = sessionStore.username

    suspend fun login(username: String, password: String, sessionDir: String) = withContext(Dispatchers.IO) {
        try {
            pythonBridge.login(username, password, sessionDir)
            sessionStore.username = username
        } catch (error: Exception) {
            if (error.toDownloadError() is DownloadError.TwoFactorRequired) {
                sessionStore.username = username
            }
            throw error
        }
    }

    suspend fun submitTwoFactor(code: String, sessionDir: String) = withContext(Dispatchers.IO) {
        val username = sessionStore.username ?: error("No pending login session")
        pythonBridge.twoFactorLogin(code, username, sessionDir)
        sessionStore.username = username
    }

    suspend fun testSession(sessionDir: String): Boolean = withContext(Dispatchers.IO) {
        val username = sessionStore.username ?: return@withContext false
        pythonBridge.testSession(username, sessionDir)
    }

    fun logout() {
        deleteSessionFiles()
        sessionStore.clear()
    }

    private fun deleteSessionFiles() {
        val directory = sessionStore.sessionDir?.let(::File)
            ?: File(context.filesDir, SESSION_DIR_NAME)
        if (!directory.isDirectory) {
            return
        }
        directory.listFiles()
            ?.filter { it.isFile && it.name.startsWith(SESSION_FILE_PREFIX) }
            ?.forEach { it.delete() }
    }

    companion object {
        private const val SESSION_DIR_NAME = "sessions"
        private const val SESSION_FILE_PREFIX = "session-"
    }
}
