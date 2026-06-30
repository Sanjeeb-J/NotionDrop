package com.sanjeeb.notiondrop.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NotionActionDark,
    secondary = NotionTextSecondaryDark,
    tertiary = NotionBorderDark,
    background = NotionBgDark,
    surface = NotionSurfaceDark,
    onPrimary = NotionBgDark,
    onSecondary = NotionBgDark,
    onTertiary = NotionTextPrimaryDark,
    onBackground = NotionTextPrimaryDark,
    onSurface = NotionTextPrimaryDark,
    surfaceVariant = NotionSurfaceDark,
    onSurfaceVariant = NotionTextSecondaryDark,
    outline = NotionBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = NotionActionLight,
    secondary = NotionTextSecondaryLight,
    tertiary = NotionBorderLight,
    background = NotionBgLight,
    surface = NotionSurfaceLight,
    onPrimary = NotionBgLight,
    onSecondary = NotionTextPrimaryLight,
    onTertiary = NotionTextPrimaryLight,
    onBackground = NotionTextPrimaryLight,
    onSurface = NotionTextPrimaryLight,
    surfaceVariant = NotionSurfaceLight,
    onSurfaceVariant = NotionTextSecondaryLight,
    outline = NotionBorderLight
)

@Composable
fun NotionDropTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce strict Notion aesthetics
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
