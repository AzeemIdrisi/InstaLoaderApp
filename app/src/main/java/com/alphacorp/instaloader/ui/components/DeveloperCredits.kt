package com.alphacorp.instaloader.ui.components

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alphacorp.instaloader.R

@Composable
fun DeveloperCredits(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.developer_github_url)
    val versionName = appVersionName(context)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (!compact) {
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = stringResource(R.string.developer_credit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (!compact) {
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedButton(
                onClick = { uriHandler.openUri(githubUrl) },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.view_github))
            }
        }
    }
}

private fun appVersionName(context: android.content.Context): String {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0),
            ).versionName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }
    }.getOrNull().orEmpty()
}
