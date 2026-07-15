package dev.anmitali.stir.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StirLightColorScheme = lightColorScheme(
    primary = StirSeaLight,
    onPrimary = StirSeaOnLight,
    primaryContainer = StirSeaContainerLight,
    onPrimaryContainer = StirSeaOnContainerLight,
    secondary = StirSandLight,
    onSecondary = StirSandOnLight,
    secondaryContainer = StirSandContainerLight,
    onSecondaryContainer = StirSandOnContainerLight,
    tertiary = StirDuskLight,
    onTertiary = StirDuskOnLight,
    tertiaryContainer = StirDuskContainerLight,
    onTertiaryContainer = StirDuskOnContainerLight,
    background = StirBackgroundLight,
    onBackground = StirOnBackgroundLight,
    surface = StirSurfaceLight,
    onSurface = StirOnSurfaceLight,
    surfaceVariant = StirSurfaceVariantLight,
    onSurfaceVariant = StirOnSurfaceVariantLight,
    outline = StirOutlineLight,
    error = StirErrorLight,
    onError = StirOnErrorLight,
    errorContainer = StirErrorContainerLight,
    onErrorContainer = StirOnErrorContainerLight,
)

private val StirDarkColorScheme = darkColorScheme(
    primary = StirSeaDark,
    onPrimary = StirSeaOnDark,
    primaryContainer = StirSeaContainerDark,
    onPrimaryContainer = StirSeaOnContainerDark,
    secondary = StirSandDark,
    onSecondary = StirSandOnDark,
    secondaryContainer = StirSandContainerDark,
    onSecondaryContainer = StirSandOnContainerDark,
    tertiary = StirDuskDark,
    onTertiary = StirDuskOnDark,
    tertiaryContainer = StirDuskContainerDark,
    onTertiaryContainer = StirDuskOnContainerDark,
    background = StirBackgroundDark,
    onBackground = StirOnBackgroundDark,
    surface = StirSurfaceDark,
    onSurface = StirOnSurfaceDark,
    surfaceVariant = StirSurfaceVariantDark,
    onSurfaceVariant = StirOnSurfaceVariantDark,
    outline = StirOutlineDark,
    error = StirErrorDark,
    onError = StirOnErrorDark,
    errorContainer = StirErrorContainerDark,
    onErrorContainer = StirOnErrorContainerDark,
)

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun StirTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && supportsDynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StirDarkColorScheme
        else -> StirLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StirTypography,
        shapes = StirShapes,
        content = content,
    )
}
