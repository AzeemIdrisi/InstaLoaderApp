package com.alphacorp.instaloader.util

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.io.File
import java.net.URLDecoder

object StorageAccess {

    const val APP_FOLDER_NAME = "InstaLoaderApp"

    enum class LocationPreset(val displayName: String) {
        DOWNLOADS("Downloads"),
        DOCUMENTS("Documents"),
        PICTURES("Pictures"),
    }

    fun defaultDownloadDirectory(context: Context): File =
        presetDownloadDirectory(context, LocationPreset.DOWNLOADS)

    fun presetDownloadDirectory(context: Context, preset: LocationPreset): File {
        val publicDir = when (preset) {
            LocationPreset.DOWNLOADS -> Environment.DIRECTORY_DOWNLOADS
            LocationPreset.DOCUMENTS -> Environment.DIRECTORY_DOCUMENTS
            LocationPreset.PICTURES -> Environment.DIRECTORY_PICTURES
        }
        return File(
            Environment.getExternalStoragePublicDirectory(publicDir),
            APP_FOLDER_NAME,
        ).also { ensureExists(it) }
    }

    fun resolveDownloadDirectory(context: Context, customPath: String?): File {
        val dir = if (customPath.isNullOrBlank()) {
            defaultDownloadDirectory(context)
        } else {
            File(customPath)
        }
        ensureExists(dir)
        return dir
    }

    fun displayPath(dir: File): String {
        val absolute = dir.absolutePath
        val downloadsRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val documentsRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
        val picturesRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath

        val friendly = when {
            absolute.startsWith(downloadsRoot) ->
                "Downloads${absolute.removePrefix(downloadsRoot)}"
            absolute.startsWith(documentsRoot) ->
                "Documents${absolute.removePrefix(documentsRoot)}"
            absolute.startsWith(picturesRoot) ->
                "Pictures${absolute.removePrefix(picturesRoot)}"
            else -> absolute
        }.replace("//", "/").trimStart('/')

        return friendly.ifBlank { APP_FOLDER_NAME }
    }

    fun locationSummary(dir: File): String = buildString {
        append(displayPath(dir))
        append('\n')
        append(dir.absolutePath)
    }

    fun matchesPreset(context: Context, dir: File, preset: LocationPreset): Boolean {
        return dir.absolutePath == presetDownloadDirectory(context, preset).absolutePath
    }

    fun isDefaultLocation(context: Context, dir: File): Boolean {
        return dir.absolutePath == defaultDownloadDirectory(context).absolutePath
    }

    fun pathFromTreeUri(uri: Uri): String? {
        if (uri.scheme != "content") return null
        val docId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return null
        if (!docId.startsWith("primary:")) return null

        val relativePath = URLDecoder.decode(docId.removePrefix("primary:"), Charsets.UTF_8.name())
        return File(Environment.getExternalStorageDirectory(), relativePath).absolutePath
    }

    fun hasWriteAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun manageStorageSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    fun legacyWritePermission(): String = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    fun needsLegacyWritePermission(): Boolean =
        Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.Q

    fun needsManageStoragePermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun scanDirectory(context: Context, directory: File) {
        if (!directory.exists()) return
        directory.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    null,
                    null,
                )
            }
    }

    private fun ensureExists(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }
}
