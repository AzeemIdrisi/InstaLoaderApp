package com.alphacorp.instaloader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphacorp.instaloader.R
import com.alphacorp.instaloader.data.model.DownloadLocationState
import com.alphacorp.instaloader.util.StorageAccess

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadLocationSection(
    location: DownloadLocationState,
    onSelectPreset: (StorageAccess.LocationPreset) -> Unit,
    onChooseFolder: () -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.download_location),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    location.summary.split("\n").forEachIndexed { index, line ->
                        Text(
                            text = line,
                            style = if (index == 0) {
                                MaterialTheme.typography.bodyMedium
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.download_location_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StorageAccess.LocationPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = location.selectedPreset == preset,
                        onClick = { onSelectPreset(preset) },
                        label = { Text(preset.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onChooseFolder) {
                    Text(stringResource(R.string.choose_folder))
                }
                if (!location.isDefault) {
                    TextButton(onClick = onResetToDefault) {
                        Text(stringResource(R.string.use_default_location))
                    }
                }
            }
        }
    }
}
