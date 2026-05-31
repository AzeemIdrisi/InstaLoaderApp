package com.alphacorp.instaloader.data.model

import com.alphacorp.instaloader.util.StorageAccess
import java.io.File

data class DownloadLocationState(
    val directory: File,
    val summary: String,
    val isDefault: Boolean,
    val selectedPreset: StorageAccess.LocationPreset?,
)
