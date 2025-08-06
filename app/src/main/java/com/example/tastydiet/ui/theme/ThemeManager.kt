package com.example.tastydiet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.tastydiet.ui.screens.ThemeMode

@Composable
fun TastyDietTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80,
            background = DarkBackground,
            surface = DarkSurface,
            onPrimary = DarkOnPrimary,
            onSecondary = DarkOnSecondary,
            onTertiary = DarkOnTertiary,
            onBackground = DarkOnBackground,
            onSurface = DarkOnSurface,
            primaryContainer = DarkPrimaryContainer,
            secondaryContainer = DarkSecondaryContainer,
            tertiaryContainer = DarkTertiaryContainer,
            onPrimaryContainer = DarkOnPrimaryContainer,
            onSecondaryContainer = DarkOnSecondaryContainer,
            onTertiaryContainer = DarkOnTertiaryContainer
        )
    } else {
        lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40,
            background = LightBackground,
            surface = LightSurface,
            onPrimary = LightOnPrimary,
            onSecondary = LightOnSecondary,
            onTertiary = LightOnTertiary,
            onBackground = LightOnBackground,
            onSurface = LightOnSurface,
            primaryContainer = LightPrimaryContainer,
            secondaryContainer = LightSecondaryContainer,
            tertiaryContainer = LightTertiaryContainer,
            onPrimaryContainer = LightOnPrimaryContainer,
            onSecondaryContainer = LightOnSecondaryContainer,
            onTertiaryContainer = LightOnTertiaryContainer
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 