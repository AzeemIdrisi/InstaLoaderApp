package com.alphacorp.instaloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alphacorp.instaloader.di.AppModule
import com.alphacorp.instaloader.service.DownloadForegroundService
import com.alphacorp.instaloader.ui.navigation.AppNavHost
import com.alphacorp.instaloader.ui.theme.InstaLoaderTheme
import com.alphacorp.instaloader.util.StorageAccess

class MainActivity : ComponentActivity() {

    private val downloadRepository by lazy { AppModule.downloadRepository(this) }
    private val sessionRepository by lazy { AppModule.sessionRepository(this) }
    private val parseInput by lazy { AppModule.parseInstagramInputUseCase() }

    private var pendingDownloadInput: String? = null

    private val legacyStorageLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            continueDownloadPermissionFlow()
        } else {
            Toast.makeText(
                this,
                getString(R.string.storage_permission_required),
                Toast.LENGTH_LONG,
            ).show()
            pendingDownloadInput = null
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        startPendingDownload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        downloadRepository.sessionDirectory()
        downloadRepository.downloadBaseDir()

        setContent {
            InstaLoaderTheme {
                AppNavHost(
                    downloadRepository = downloadRepository,
                    sessionRepository = sessionRepository,
                    parseInput = parseInput,
                    onStartDownload = ::startDownloadWithPermissions,
                    onCancelDownload = {
                        DownloadForegroundService.cancel(this)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingDownloadInput != null && StorageAccess.hasWriteAccess(this)) {
            continueDownloadPermissionFlow()
        }
    }

    private fun startDownloadWithPermissions(input: String) {
        pendingDownloadInput = input
        when {
            StorageAccess.needsLegacyWritePermission() &&
                !StorageAccess.hasWriteAccess(this) -> {
                legacyStorageLauncher.launch(StorageAccess.legacyWritePermission())
            }

            StorageAccess.needsManageStoragePermission() &&
                !StorageAccess.hasWriteAccess(this) -> {
                Toast.makeText(
                    this,
                    getString(R.string.storage_permission_required),
                    Toast.LENGTH_LONG,
                ).show()
                startActivity(StorageAccess.manageStorageSettingsIntent(this))
            }

            else -> continueDownloadPermissionFlow()
        }
    }

    private fun continueDownloadPermissionFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startPendingDownload()
        }
    }

    private fun startPendingDownload() {
        pendingDownloadInput?.let { input ->
            DownloadForegroundService.start(this, input)
        }
        pendingDownloadInput = null
    }
}
