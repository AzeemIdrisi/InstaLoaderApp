package com.alphacorp.instaloader.ui.home

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alphacorp.instaloader.R

@Composable
fun HomeHeroHeader(
    onOpenLogin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(R.string.home_tagline),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
                Row {
                    IconButton(onClick = onOpenLogin) {
                        Icon(
                            Icons.AutoMirrored.Filled.Login,
                            contentDescription = stringResource(R.string.login),
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeaturePill(text = stringResource(R.string.home_feature_profiles))
                FeaturePill(text = stringResource(R.string.home_feature_posts))
                FeaturePill(text = stringResource(R.string.home_feature_hashtags))
            }
        }
    }
}

@Composable
private fun FeaturePill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.22f),
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun HomeInputCard(
    input: String,
    targetLabel: String,
    enabled: Boolean,
    onInputChanged: (String) -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.home_input_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.home_input_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                label = { Text(stringResource(R.string.input_label)) },
                placeholder = { Text(stringResource(R.string.input_placeholder)) },
                singleLine = false,
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = onPaste, enabled = enabled) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = stringResource(R.string.paste),
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            if (targetLabel.isNotBlank() && input.isNotBlank()) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text("${stringResource(R.string.detected)}: $targetLabel")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = targetIcon(targetLabel),
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                            tint = Color.White,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = Color.White,
                        leadingIconContentColor = Color.White,
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    ),
                )
            }
        }
    }
}

@Composable
fun HomeDownloadButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            Icons.Default.Download,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = stringResource(R.string.start_download),
            modifier = Modifier.padding(start = 10.dp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun HomeCancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(stringResource(R.string.cancel_download))
    }
}

private fun targetIcon(targetLabel: String): ImageVector {
    return when {
        targetLabel.contains("Profile", ignoreCase = true) -> Icons.Default.Person
        targetLabel.contains("Hashtag", ignoreCase = true) -> Icons.Default.Tag
        else -> Icons.Default.Image
    }
}

fun pasteFromClipboard(context: Context): String {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
}
