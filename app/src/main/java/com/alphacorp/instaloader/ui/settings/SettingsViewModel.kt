package com.alphacorp.instaloader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alphacorp.instaloader.data.model.DownloadLocationState
import com.alphacorp.instaloader.data.model.DownloadOptions
import com.alphacorp.instaloader.data.repository.DownloadRepository
import com.alphacorp.instaloader.util.StorageAccess
import java.io.File
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    val options: StateFlow<DownloadOptions> = downloadRepository.options.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DownloadOptions.Default,
    )

    val downloadLocation: StateFlow<DownloadLocationState> = downloadRepository.downloadDirectory
        .map(downloadRepository::downloadLocationState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DownloadLocationState(
                directory = File(""),
                summary = "",
                isDefault = true,
                selectedPreset = StorageAccess.LocationPreset.DOWNLOADS,
            ),
        )

    fun updateOptions(transform: (DownloadOptions) -> DownloadOptions) {
        viewModelScope.launch {
            downloadRepository.saveOptions(transform(options.value))
        }
    }

    fun resetOptions() {
        viewModelScope.launch {
            downloadRepository.resetOptions()
        }
    }

    fun selectPreset(preset: StorageAccess.LocationPreset) {
        viewModelScope.launch {
            downloadRepository.savePresetLocation(preset)
        }
    }

    fun setCustomFolderPath(path: String) {
        viewModelScope.launch {
            downloadRepository.saveDownloadLocation(path)
        }
    }

    fun resetDownloadLocation() {
        viewModelScope.launch {
            downloadRepository.resetDownloadLocation()
        }
    }

    class Factory(
        private val downloadRepository: DownloadRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(downloadRepository) as T
        }
    }
}
