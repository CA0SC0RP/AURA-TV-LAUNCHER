package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvDarkColorScheme = darkColorScheme(
  primary = NeonBlue,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF0369A1),
  onPrimaryContainer = Color.White,
  secondary = NeonPurple,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF6B21A8),
  onSecondaryContainer = Color.White,
  tertiary = NeonAmber,
  background = TvBackground,
  onBackground = TvTextPrimary,
  surface = TvSurface,
  onSurface = TvTextPrimary,
  surfaceVariant = TvSurfaceVariant,
  onSurfaceVariant = TvTextSecondary,
  outline = TvBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = TvDarkColorScheme,
    typography = Typography,
    content = content
  )
}
