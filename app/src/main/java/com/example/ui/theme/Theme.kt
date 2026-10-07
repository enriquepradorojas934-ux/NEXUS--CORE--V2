package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = ExecutivePrimaryTeal,
  onPrimary = Color.Black,
  primaryContainer = ExecutivePrimaryTealContainer,
  onPrimaryContainer = Color.White,
  secondary = ExecutiveSecondaryAmber,
  onSecondary = Color.Black,
  tertiary = ExecutiveCyan,
  background = ExecutiveDarkBg,
  surface = ExecutiveDarkSurface,
  surfaceVariant = ExecutiveDarkSurfaceVariant,
  surfaceContainer = ExecutiveDarkSurfaceElevated,
  outline = ExecutiveBorder,
  outlineVariant = ExecutiveBorderSubtle,
  onBackground = ExecutiveTextWhite,
  onSurface = ExecutiveTextWhite,
  onSurfaceVariant = ExecutiveTextMuted,
  error = ExecutiveDangerRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF0D9488),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFCCFBF1),
  onPrimaryContainer = Color(0xFF134E4A),
  secondary = Color(0xFFD97706),
  tertiary = Color(0xFF0284C7),
  background = Color(0xFFF8FAFC),
  surface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xFFF1F5F9),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  onSurfaceVariant = Color(0xFF64748B),
  error = Color(0xFFDC2626),
  onError = Color.White
)

fun getAccentColor(name: String): Color {
    return when (name) {
        "Púrpura" -> SoloNeonPurple
        "Zafiro" -> SoloNeonCyan
        "Ámbar" -> SoloGold
        "Esmeralda" -> ExecutiveSuccessGreen
        else -> SoloNeonCyan
    }
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek executive dark palette
  dynamicColor: Boolean = false,
  accentColorName: String = "Teal",
  content: @Composable () -> Unit,
) {
  val accent = getAccentColor(accentColorName)
  val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val colorScheme = baseScheme.copy(
    primary = accent,
    primaryContainer = accent.copy(alpha = 0.25f)
  )
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
