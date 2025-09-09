package io.github.numq.cameracapture.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF46474B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF49484B),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFF312C31),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF48474A),
    onSecondaryContainer = Color(0xFFFFFFFF),

    tertiary = Color(0xFF000000),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF46474B),
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFF46474B),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF46474B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF312C31),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFF49484B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF48474A),
    onSecondaryContainer = Color(0xFFFFFFFF),

    tertiary = Color(0xFF000000),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF312C31),
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFF000000),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF312C31),
    onSurfaceVariant = Color(0xFFC6C6C6),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)