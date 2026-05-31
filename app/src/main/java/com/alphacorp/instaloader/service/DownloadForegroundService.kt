package com.alphacorp.instaloader.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.alphacorp.instaloader.MainActivity
import com.alphacorp.instaloader.R
import com.alphacorp.instaloader.data.model.DownloadProgress
import com.alphacorp.instaloader.data.model.toDownloadError
import com.alphacorp.instaloader.data.repository.DownloadRepository
import com.alphacorp.instaloader.di.AppModule
import com.alphacorp.instaloader.util.StorageAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var downloadRepository: DownloadRepository
    private var downloadJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        downloadRepository = AppModule.downloadRepository(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                downloadRepository.cancelDownload()
                downloadJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            ACTION_START -> {
                promoteToForeground("Preparing download")
                val input = intent.getStringExtra(EXTRA_INPUT).orEmpty()
                if (input.isBlank()) {
                    reportError("No download target provided.")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
                startDownload(input)
            }
        }
        return START_NOT_STICKY
    }

    private fun startDownload(input: String) {
        downloadJob?.cancel()
        downloadRepository.sessionDirectory()

        downloadJob = serviceScope.launch {
            val progressJob = launch {
                downloadRepository.progress.collect { progress ->
                    val title = progress.message.ifBlank {
                        progress.phase.replaceFirstChar { it.uppercase() }
                    }
                    updateNotification(title, progress.completed, progress.total, progress.progressFraction)
                }
            }

            runCatching {
                val options = downloadRepository.options.first()
                downloadRepository.startDownload(input, options)
            }.onSuccess {
                StorageAccess.scanDirectory(
                    this@DownloadForegroundService,
                    downloadRepository.downloadBaseDir(),
                )
            }.onFailure { error ->
                Log.e(TAG, "Download failed", error)
                val message = error.toDownloadError().userMessage
                reportError(message)
                updateNotification(message, 0, null, null)
            }

            progressJob.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun reportError(message: String) {
        downloadRepository.updateProgress(
            DownloadProgress(phase = "error", message = message),
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun promoteToForeground(content: String) {
        val notification = buildNotification(content, 0, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.download_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        content: String,
        completed: Int,
        fraction: Float?,
    ): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val cancelIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, DownloadForegroundService::class.java).setAction(ACTION_CANCEL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(0, getString(R.string.cancel_download), cancelIntent)

        if (fraction != null) {
            builder.setProgress(100, (fraction * 100).toInt(), false)
        } else {
            builder.setProgress(0, 0, true)
        }

        if (completed > 0) {
            builder.setSubText(getString(R.string.notification_completed, completed))
        }

        return builder.build()
    }

    private fun updateNotification(
        content: String,
        completed: Int,
        total: Int?,
        fraction: Float?,
    ) {
        val manager = getSystemService(NotificationManager::class.java)
        val text = if (total != null) "$content ($completed/$total)" else content
        manager.notify(NOTIFICATION_ID, buildNotification(text, completed, fraction))
    }

    companion object {
        private const val TAG = "DownloadService"
        const val ACTION_START = "com.alphacorp.instaloader.action.START_DOWNLOAD"
        const val ACTION_CANCEL = "com.alphacorp.instaloader.action.CANCEL_DOWNLOAD"
        const val EXTRA_INPUT = "extra_input"
        private const val CHANNEL_ID = "download_progress"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context, input: String) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_INPUT, input)
            }
            context.startForegroundService(intent)
        }

        fun cancel(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }
}
