package com.alphacorp.instaloader.data.repository

import com.alphacorp.instaloader.data.local.EncryptedSessionStore
import com.alphacorp.instaloader.data.python.PythonBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionRepository(
    private val pythonBridge: PythonBridge,
    private val sessionStore: EncryptedSessionStore,
) {
    val username: String?
        get() = sessionStore.username

    suspend fun login(username: String, password: String, sessionDir: String) = withContext(Dispatchers.IO) {
        pythonBridge.login(username, password, sessionDir)
        sessionStore.username = username
    }

    suspend fun submitTwoFactor(code: String, sessionDir: String) = withContext(Dispatchers.IO) {
        val username = sessionStore.username ?: error("No pending login session")
        pythonBridge.twoFactorLogin(code, username, sessionDir)
    }

    suspend fun testSession(sessionDir: String): Boolean = withContext(Dispatchers.IO) {
        val username = sessionStore.username ?: return@withContext false
        pythonBridge.testSession(username, sessionDir)
    }

    fun logout() {
        sessionStore.clear()
    }
}
