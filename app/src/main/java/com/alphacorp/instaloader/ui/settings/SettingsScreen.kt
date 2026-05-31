package com.alphacorp.instaloader.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alphacorp.instaloader.R
import com.alphacorp.instaloader.ui.components.DeveloperCredits
import com.alphacorp.instaloader.ui.components.DownloadLocationSection
import com.alphacorp.instaloader.ui.components.OptionSwitch
import com.alphacorp.instaloader.ui.components.OptionTextField
import com.alphacorp.instaloader.ui.components.SettingsSectionTitle
import com.alphacorp.instaloader.util.StorageAccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val options by viewModel.options.collectAsStateWithLifecycle()
    val downloadLocation by viewModel.downloadLocation.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let { treeUri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            StorageAccess.pathFromTreeUri(treeUri)?.let(viewModel::setCustomFolderPath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name))
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionTitle(stringResource(R.string.section_media))
            OptionSwitch("Photos", "Download pictures", options.downloadPictures, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(downloadPictures = enabled) }
            })
            OptionSwitch("Videos", "Download videos", options.downloadVideos, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(downloadVideos = enabled) }
            })
            OptionSwitch("Video thumbnails", "Save video preview images", options.downloadVideoThumbnails, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(downloadVideoThumbnails = enabled) }
            })
            OptionTextField("Slide range", "Only download selected sidecar slides (e.g. 1-3)", options.slide, onValueChange = { value ->
                viewModel.updateOptions { it.copy(slide = value) }
            })

            SettingsSectionTitle(stringResource(R.string.section_metadata))
            OptionSwitch("JSON metadata", "Save .json.xz metadata files", options.saveMetadata, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(saveMetadata = enabled) }
            })
            OptionSwitch("Compress JSON", "Store metadata as compressed JSON", options.compressJson, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(compressJson = enabled) }
            })
            OptionSwitch("Captions", "Save caption text files", options.saveCaptions, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(saveCaptions = enabled) }
            })
            OptionTextField("Caption pattern", "Template for caption files", options.postMetadataTxtPattern, onValueChange = { value ->
                viewModel.updateOptions { it.copy(postMetadataTxtPattern = value) }
            })
            OptionTextField("Story metadata pattern", "Template for story text files", options.storyitemMetadataTxtPattern, onValueChange = { value ->
                viewModel.updateOptions { it.copy(storyitemMetadataTxtPattern = value) }
            })
            OptionSwitch("Geotags", "Download location metadata", options.downloadGeotags, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(downloadGeotags = enabled) }
            })
            OptionSwitch("Comments", "Download comments (requires login)", options.downloadComments, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(downloadComments = enabled) }
            })

            SettingsSectionTitle(stringResource(R.string.section_profile_content))
            OptionSwitch("Profile picture", "Download profile picture", options.profilePic, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(profilePic = enabled) }
            })
            OptionSwitch("Posts", "Download regular posts", options.posts, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(posts = enabled) }
            })
            OptionSwitch("Stories", "Download stories (requires login)", options.stories, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(stories = enabled) }
            })
            OptionSwitch("Highlights", "Download highlights (requires login)", options.highlights, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(highlights = enabled) }
            })
            OptionSwitch("Reels", "Download reels tab videos", options.reels, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(reels = enabled) }
            })
            OptionSwitch("IGTV", "Download long-form IGTV videos", options.igtv, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(igtv = enabled) }
            })
            OptionSwitch("Tagged posts", "Download posts where profile is tagged", options.tagged, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(tagged = enabled) }
            })

            SettingsSectionTitle(stringResource(R.string.section_filters))
            OptionSwitch("Fast update", "Stop at first already-downloaded item", options.fastUpdate, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(fastUpdate = enabled) }
            })
            OptionTextField("Max count", "Maximum number of posts to download", options.maxCount?.toString().orEmpty(), onValueChange = { value ->
                viewModel.updateOptions { it.copy(maxCount = value.toIntOrNull()) }
            })
            OptionTextField("Post filter", "e.g. not is_video or likes > 100", options.postFilter, onValueChange = { value ->
                viewModel.updateOptions { it.copy(postFilter = value) }
            })
            OptionTextField("Story filter", "e.g. not is_video", options.storyitemFilter, onValueChange = { value ->
                viewModel.updateOptions { it.copy(storyitemFilter = value) }
            })

            SettingsSectionTitle(stringResource(R.string.section_storage))
            DownloadLocationSection(
                location = downloadLocation,
                onSelectPreset = viewModel::selectPreset,
                onChooseFolder = { folderPickerLauncher.launch(null) },
                onResetToDefault = viewModel::resetDownloadLocation,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            SettingsSectionTitle(stringResource(R.string.section_paths))
            OptionTextField("Directory pattern", "Folder naming template", options.dirnamePattern, onValueChange = { value ->
                viewModel.updateOptions { it.copy(dirnamePattern = value) }
            })
            OptionTextField("Filename pattern", "File naming template", options.filenamePattern, onValueChange = { value ->
                viewModel.updateOptions { it.copy(filenamePattern = value) }
            })
            OptionTextField("Title pattern", "Title image naming template", options.titlePattern, onValueChange = { value ->
                viewModel.updateOptions { it.copy(titlePattern = value) }
            })
            OptionSwitch("Sanitize paths", "Use cross-platform safe names", options.sanitizePaths, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(sanitizePaths = enabled) }
            })

            SettingsSectionTitle(stringResource(R.string.section_network))
            OptionSwitch("Quiet mode", "Suppress console output", options.quiet, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(quiet = enabled) }
            })
            OptionTextField("User agent", "Custom HTTP user agent", options.userAgent, onValueChange = { value ->
                viewModel.updateOptions { it.copy(userAgent = value) }
            })
            OptionTextField("Max connection attempts", "Retry count for failed requests", options.maxConnectionAttempts.toString(), onValueChange = { value ->
                viewModel.updateOptions { it.copy(maxConnectionAttempts = value.toIntOrNull() ?: 3) }
            })
            OptionTextField("Request timeout", "Per-request timeout in seconds", options.requestTimeout.toInt().toString(), onValueChange = { value ->
                viewModel.updateOptions { it.copy(requestTimeout = value.toDoubleOrNull() ?: 300.0) }
            })
            OptionSwitch("iPhone support", "Use iPhone-quality media URLs", options.iphoneSupport, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(iphoneSupport = enabled) }
            })
            OptionSwitch("Resume downloads", "Use JSON resume checkpoints", options.enableResume, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(enableResume = enabled) }
            })
            OptionTextField("Resume prefix", "Prefix for resume checkpoint files", options.resumePrefix, onValueChange = { value ->
                viewModel.updateOptions { it.copy(resumePrefix = value) }
            })
            OptionSwitch("Check resume expiry", "Reject expired resume files", options.checkResumeBbd, onCheckedChange = { enabled ->
                viewModel.updateOptions { it.copy(checkResumeBbd = enabled) }
            })

            OutlinedButton(
                onClick = viewModel::resetOptions,
                modifier = Modifier.padding(vertical = 24.dp),
            ) {
                Text(stringResource(R.string.reset_settings))
            }

            DeveloperCredits(
                modifier = Modifier.padding(bottom = 32.dp),
            )
        }
    }
}
