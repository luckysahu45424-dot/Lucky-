package com.example.ui.theme

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

private val DarkColorScheme =
  lightColorScheme(
    primary = Primary,
    secondary = AccentBlue,
    tertiary = Tertiary,
    background = DarkBG,
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = GridBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Primary,
    secondary = AccentBlue,
    tertiary = Tertiary,
    background = DarkBG,
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = GridBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme or default to true for the premium dark vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to enforce the brand aesthetics
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
