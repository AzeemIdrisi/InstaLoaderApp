package com.alphacorp.instaloader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alphacorp.instaloader.data.model.DownloadError
import com.alphacorp.instaloader.data.model.DownloadProgress
import com.alphacorp.instaloader.data.model.toDownloadError
import com.alphacorp.instaloader.data.repository.DownloadRepository
import com.alphacorp.instaloader.domain.usecase.ParseInstagramInputUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val input: String = "",
    val targetLabel: String = "",
    val progress: DownloadProgress = DownloadProgress(),
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class HomeViewModel(
    private val downloadRepository: DownloadRepository,
    private val parseInput: ParseInstagramInputUseCase,
    private val onStartDownload: (String) -> Unit,
    private val onCancelDownload: () -> Unit,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val options = downloadRepository.options.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = com.alphacorp.instaloader.data.model.DownloadOptions.Default,
    )

    init {
        viewModelScope.launch {
            downloadRepository.progress.collect { progress ->
                _uiState.value = _uiState.value.copy(
                    progress = progress,
                    isDownloading = progress.isActive,
                    successMessage = if (progress.phase == "finished") "Download finished" else null,
                    errorMessage = when (progress.phase) {
                        "error" -> progress.message.ifBlank { "Download failed." }
                        "cancelled" -> "Download cancelled."
                        else -> null
                    },
                )
            }
        }
    }

    fun onInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            input = value,
            targetLabel = parseInput.detectLabel(value),
            errorMessage = null,
            successMessage = null,
        )
    }

    fun pasteInput(value: String) {
        onInputChanged(value)
    }

    fun startDownload() {
        val input = _uiState.value.input.trim()
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a username, hashtag, or Instagram URL.")
            return
        }

        runCatching { parseInput(input) }
            .onFailure { error ->
                val message = (error as? DownloadError)?.userMessage ?: error.toDownloadError().userMessage
                _uiState.value = _uiState.value.copy(errorMessage = message)
            }
            .onSuccess {
                _uiState.value = _uiState.value.copy(
                    errorMessage = null,
                    successMessage = null,
                    isDownloading = true,
                )
                onStartDownload(input)
            }
    }

    fun cancelDownload() {
        onCancelDownload()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
        downloadRepository.clearProgress()
    }

    class Factory(
        private val downloadRepository: DownloadRepository,
        private val parseInput: ParseInstagramInputUseCase,
        private val onStartDownload: (String) -> Unit,
        private val onCancelDownload: () -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                downloadRepository,
                parseInput,
                onStartDownload,
                onCancelDownload,
            ) as T
        }
    }
}
