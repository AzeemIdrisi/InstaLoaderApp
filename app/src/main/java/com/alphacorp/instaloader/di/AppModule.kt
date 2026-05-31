package com.alphacorp.instaloader.di

import android.content.Context
import com.alphacorp.instaloader.data.local.DownloadLocationStore
import com.alphacorp.instaloader.data.local.EncryptedSessionStore
import com.alphacorp.instaloader.data.local.OptionsStore
import com.alphacorp.instaloader.data.python.PythonBridge
import com.alphacorp.instaloader.data.repository.DownloadRepository
import com.alphacorp.instaloader.data.repository.SessionRepository
import com.alphacorp.instaloader.domain.usecase.ParseInstagramInputUseCase

object AppModule {

    private var downloadRepository: DownloadRepository? = null
    private var sessionRepository: SessionRepository? = null

    fun downloadRepository(context: Context): DownloadRepository {
        return downloadRepository ?: synchronized(this) {
            downloadRepository ?: createDownloadRepository(context.applicationContext).also {
                downloadRepository = it
            }
        }
    }

    fun sessionRepository(context: Context): SessionRepository {
        return sessionRepository ?: synchronized(this) {
            sessionRepository ?: createSessionRepository(context.applicationContext).also {
                sessionRepository = it
            }
        }
    }

    fun parseInstagramInputUseCase(): ParseInstagramInputUseCase = ParseInstagramInputUseCase()

    private fun createDownloadRepository(context: Context): DownloadRepository {
        val optionsStore = OptionsStore(context)
        val downloadLocationStore = DownloadLocationStore(context)
        val sessionStore = EncryptedSessionStore(context)
        val pythonBridge = PythonBridge()
        val parseInput = ParseInstagramInputUseCase()
        return DownloadRepository(
            context = context,
            pythonBridge = pythonBridge,
            optionsStore = optionsStore,
            downloadLocationStore = downloadLocationStore,
            sessionStore = sessionStore,
            parseInput = parseInput,
        )
    }

    private fun createSessionRepository(context: Context): SessionRepository {
        val sessionStore = EncryptedSessionStore(context)
        val pythonBridge = PythonBridge()
        return SessionRepository(
            context = context,
            pythonBridge = pythonBridge,
            sessionStore = sessionStore,
        )
    }
}
