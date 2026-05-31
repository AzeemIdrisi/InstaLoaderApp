package com.alphacorp.instaloader.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alphacorp.instaloader.ui.components.DeveloperCredits
import com.alphacorp.instaloader.ui.components.DownloadProgressCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            HomeHeroHeader(
                onOpenLogin = onOpenLogin,
                onOpenSettings = onOpenSettings,
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-14).dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                HomeInputCard(
                    input = uiState.input,
                    targetLabel = uiState.targetLabel,
                    enabled = !uiState.isDownloading,
                    onInputChanged = viewModel::onInputChanged,
                    onPaste = {
                        val text = pasteFromClipboard(context)
                        if (text.isNotBlank()) {
                            viewModel.pasteInput(text)
                        }
                    },
                )

                if (uiState.isDownloading || uiState.progress.phase in setOf("finished", "error", "cancelled")) {
                    DownloadProgressCard(progress = uiState.progress)
                }

                HomeDownloadButton(
                    enabled = !uiState.isDownloading,
                    onClick = viewModel::startDownload,
                )

                if (uiState.isDownloading) {
                    HomeCancelButton(onClick = viewModel::cancelDownload)
                }

                DeveloperCredits(
                    compact = true,
                    modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
                )
            }
        }
    }
}
