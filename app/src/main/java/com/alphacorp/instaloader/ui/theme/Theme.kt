package com.alphacorp.instaloader.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandBlueDark,
    onSecondary = Color.White,
    tertiary = BrandAccent,
    onTertiary = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = BrandBlueLight,
    secondary = BrandAccent,
    onSecondary = Color.White,
    tertiary = BrandBlueLight,
    onTertiary = BrandBlueDark,
)

@Composable
fun InstaLoaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
