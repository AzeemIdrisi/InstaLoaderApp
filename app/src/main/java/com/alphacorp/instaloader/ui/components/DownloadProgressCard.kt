package com.alphacorp.instaloader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alphacorp.instaloader.data.model.DownloadProgress

@Composable
fun DownloadProgressCard(
    progress: DownloadProgress,
    modifier: Modifier = Modifier,
) {
    val phaseStyle = progressPhaseStyle(progress.phase)
    val processed = progress.completed + progress.failed + progress.skipped
    val remaining = progress.remaining ?: progress.total?.let { total ->
        (total - processed).coerceAtLeast(0)
    }
    val determinateFraction = progress.progressFraction
        ?.coerceIn(0f, 1f)
        ?.takeIf { progress.phase !in setOf("starting", "idle") }
    val statusTitle = progress.message.ifBlank {
        progress.phase.replaceFirstChar { it.uppercase() }
    }
    val countLabel = progress.total?.let { total ->
        "$processed / $total"
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(phaseStyle.containerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    if (phaseStyle.showSpinner) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = phaseStyle.contentColor,
                        )
                    } else {
                        Icon(
                            imageVector = phaseStyle.icon,
                            contentDescription = null,
                            tint = phaseStyle.contentColor,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (countLabel != null) {
                        Text(
                            text = countLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                determinateFraction?.let { fraction ->
                    Text(
                        text = "${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (determinateFraction != null) {
                LinearProgressIndicator(
                    progress = { determinateFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            } else if (progress.isActive) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }

            if (progress.total != null || processed > 0 || progress.failed > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Done",
                        value = progress.completed,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Left",
                        value = remaining,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Failed",
                        value = progress.failed,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            if (progress.current.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Current item",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = progress.current,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: Int?,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value?.toString() ?: "0",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.82f),
            textAlign = TextAlign.Center,
        )
    }
}

private data class PhaseStyle(
    val icon: ImageVector,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val showSpinner: Boolean = false,
)

@Composable
private fun progressPhaseStyle(phase: String): PhaseStyle {
    return when (phase) {
        "finished" -> PhaseStyle(
            icon = Icons.Default.CheckCircle,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        "error" -> PhaseStyle(
            icon = Icons.Default.ErrorOutline,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        "cancelled" -> PhaseStyle(
            icon = Icons.Default.Close,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        "starting", "idle" -> PhaseStyle(
            icon = Icons.Default.Download,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            showSpinner = true,
        )
        else -> PhaseStyle(
            icon = Icons.Default.HourglassEmpty,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        )
    }
}
