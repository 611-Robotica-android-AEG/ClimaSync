package com.example.climasyncapp.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF003062),
    primaryContainer = DarkContainer,
    onPrimaryContainer = Color(0xFFD6EAFF),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF1A3A5C),
    onSecondaryContainer = Color(0xFFCCE5FF),
    background = DarkBackground,
    onBackground = Color(0xFFE2E2E6),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFC8D6)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlueContainer,
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE5FF),
    onSecondaryContainer = Color(0xFF001E31),
    background = LightBackground,
    onBackground = Color(0xFF1A1C1E),
    surface = LightSurface,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF44474F)
)
@Composable
fun ClimaSyncAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}