package com.alphacorp.instaloader.data.repository

import android.content.Context
import com.alphacorp.instaloader.data.local.DownloadLocationStore
import com.alphacorp.instaloader.data.local.EncryptedSessionStore
import com.alphacorp.instaloader.data.local.OptionsStore
import com.alphacorp.instaloader.data.model.DownloadLocationState
import com.alphacorp.instaloader.data.model.DownloadOptions
import com.alphacorp.instaloader.data.model.DownloadProgress
import com.alphacorp.instaloader.data.model.DownloadTarget
import com.alphacorp.instaloader.data.model.DownloadTargetType
import com.alphacorp.instaloader.data.model.toDownloadError
import com.alphacorp.instaloader.data.python.PythonBridge
import com.alphacorp.instaloader.domain.usecase.ParseInstagramInputUseCase
import com.alphacorp.instaloader.util.StorageAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class DownloadRepository(
    private val context: Context,
    private val pythonBridge: PythonBridge,
    private val optionsStore: OptionsStore,
    private val downloadLocationStore: DownloadLocationStore,
    private val sessionStore: EncryptedSessionStore,
    private val parseInput: ParseInstagramInputUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val customPathRef = AtomicReference<String?>(null)

    private val _progress = MutableStateFlow(DownloadProgress())
    val progress: Flow<DownloadProgress> = _progress.asStateFlow()

    val options: Flow<DownloadOptions> = optionsStore.options

    val customDownloadPath: Flow<String?> = downloadLocationStore.customPath

    val downloadDirectory: Flow<File> = customDownloadPath.map { path ->
        StorageAccess.resolveDownloadDirectory(context, path)
    }

    init {
        scope.launch {
            downloadLocationStore.customPath.collect { path ->
                customPathRef.set(path)
            }
        }
    }

    fun downloadBaseDir(): File =
        StorageAccess.resolveDownloadDirectory(context, customPathRef.get())

    fun downloadLocationSummary(): String =
        StorageAccess.locationSummary(downloadBaseDir())

    fun sessionDirectory(): File {
        val dir = File(context.filesDir, "sessions").apply { mkdirs() }
        sessionStore.initializeSessionDir(dir.absolutePath)
        return dir
    }

    suspend fun saveOptions(options: DownloadOptions) {
        optionsStore.save(options)
    }

    suspend fun resetOptions() {
        optionsStore.reset()
    }

    fun downloadLocationState(directory: File): DownloadLocationState = DownloadLocationState(
        directory = directory,
        summary = StorageAccess.locationSummary(directory),
        isDefault = StorageAccess.isDefaultLocation(context, directory),
        selectedPreset = StorageAccess.LocationPreset.entries.firstOrNull { preset ->
            StorageAccess.matchesPreset(context, directory, preset)
        },
    )

    suspend fun saveDownloadLocation(path: String?) {
        downloadLocationStore.saveCustomPath(path)
        customPathRef.set(path)
        StorageAccess.resolveDownloadDirectory(context, path)
    }

    suspend fun savePresetLocation(preset: StorageAccess.LocationPreset) {
        saveDownloadLocation(
            StorageAccess.presetDownloadDirectory(context, preset).absolutePath,
        )
    }

    suspend fun resetDownloadLocation() {
        downloadLocationStore.clear()
        customPathRef.set(null)
        StorageAccess.defaultDownloadDirectory(context)
    }

    suspend fun getPostCount(username: String): Int = withContext(Dispatchers.IO) {
        pythonBridge.getPostCount(
            username = username,
            sessionUsername = sessionStore.username,
            sessionDir = sessionStore.sessionDir,
        )
    }

    suspend fun startDownload(input: String, options: DownloadOptions) = withContext(Dispatchers.IO) {
        val target = parseInput(input)
        val targetType = when (target) {
            is DownloadTarget.Profile -> DownloadTargetType.PROFILE
            is DownloadTarget.Post -> DownloadTargetType.POST
            is DownloadTarget.Hashtag -> DownloadTargetType.HASHTAG
        }
        val targetValue = when (target) {
            is DownloadTarget.Profile -> target.username
            is DownloadTarget.Post -> target.shortcode
            is DownloadTarget.Hashtag -> target.tag
        }

        _progress.value = DownloadProgress(phase = "starting", message = "Preparing download")
        try {
            pythonBridge.downloadWithOptions(
                targetType = targetType,
                targetValue = targetValue,
                options = options,
                baseDir = downloadBaseDir().absolutePath,
                sessionUsername = sessionStore.username,
                sessionDir = sessionStore.sessionDir,
                onProgress = { update ->
                    _progress.value = update
                },
            )
        } catch (error: Exception) {
            val message = error.toDownloadError().userMessage
            _progress.value = DownloadProgress(phase = "error", message = message)
            throw error
        }
    }

    fun cancelDownload() {
        pythonBridge.cancelDownload()
    }

    fun clearProgress() {
        _progress.value = DownloadProgress()
    }

    fun updateProgress(progress: DownloadProgress) {
        _progress.value = progress
    }
}
