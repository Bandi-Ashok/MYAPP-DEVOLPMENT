package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SophisticatedColorScheme = darkColorScheme(
    primary = SophisticatedAccent,
    onPrimary = SophisticatedOnAccent,
    secondary = SophisticatedAccentSecondary,
    onSecondary = SophisticatedOnAccentSecondary,
    background = SophisticatedBackground,
    onBackground = SophisticatedText,
    surface = SophisticatedSurface,
    onSurface = SophisticatedText,
    outline = SophisticatedBorder,
    surfaceVariant = SophisticatedItemBg,
    onSurfaceVariant = SophisticatedTextAlt
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for "Sophisticated Dark" request
  dynamicColor: Boolean = false, // Disable dynamic colors to enforce branding
  content: @Composable () -> Unit,
) {
  val colorScheme = SophisticatedColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
