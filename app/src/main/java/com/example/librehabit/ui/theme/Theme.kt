package com.example.librehabit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.librehabit.model.AppTheme

private val DarkPurpleColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
private val LightPurpleColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val DarkGreenColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = LightGreen80
)
private val LightGreenColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = LightGreen40
)

@Composable
fun LibreHabitTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    selectedTheme: AppTheme = AppTheme.PURPLE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        selectedTheme == AppTheme.SYSTEM_DYNAMIC && isDynamicAvailable -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        selectedTheme == AppTheme.FOREST_GREEN -> {
            if (useDarkTheme) DarkGreenColorScheme else LightGreenColorScheme
        }
        else -> { // Default to Purple
            if (useDarkTheme) DarkPurpleColorScheme else LightPurpleColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}