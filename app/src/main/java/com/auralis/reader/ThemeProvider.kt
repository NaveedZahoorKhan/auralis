package com.auralis.reader

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val key: String, val title: String, val description: String) {
    SYSTEM("system", "System Default", "Follow device appearance settings"),
    LIGHT("light", "Light Mode", "Crisp, warm paper and clean contrast"),
    DARK("dark", "Dark Mode", "Midnight slate and soothing reading contrast");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}

@Stable
interface ThemeController {
    val themeMode: AppThemeMode
    val isDark: Boolean
    fun setThemeMode(mode: AppThemeMode)
    fun toggleNextTheme()
}

val LocalThemeController = compositionLocalOf<ThemeController> {
    error("LocalThemeController not provided")
}

class ThemePreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auralis_theme_prefs", Context.MODE_PRIVATE)
    private val _themeModeFlow = MutableStateFlow(loadThemeMode())
    val themeModeFlow: StateFlow<AppThemeMode> = _themeModeFlow.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val savedKey = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.key)
        return AppThemeMode.fromKey(savedKey)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
        _themeModeFlow.value = mode
    }

    companion object {
        private const val KEY_THEME_MODE = "app_theme_mode"

        @Volatile
        private var instance: ThemePreferencesManager? = null

        fun get(context: Context): ThemePreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

private val AuralisLightColorScheme = lightColorScheme(
    primary = Color(0xFF22675E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EAE4),
    onPrimaryContainer = Color(0xFF09201C),
    secondary = Color(0xFF756041),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4E4C6),
    onSecondaryContainer = Color(0xFF261D0A),
    tertiary = Color(0xFF964B3A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD2),
    onTertiaryContainer = Color(0xFF381006),
    background = Color(0xFFFAFBF8),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFE2E8E4),
    onSurfaceVariant = Color(0xFF424946),
    surfaceContainer = Color(0xFFF1F5F2),
    surfaceContainerHigh = Color(0xFFE8EDE9),
    outline = Color(0xFF727976),
    outlineVariant = Color(0xFFC2C9C5),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val AuralisDarkColorScheme = darkColorScheme(
    primary = Color(0xFF5DD9CA),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF114F47),
    onPrimaryContainer = Color(0xFF9BF4E7),
    secondary = Color(0xFFDDC39F),
    onSecondary = Color(0xFF3E2D16),
    secondaryContainer = Color(0xFF55432B),
    onSecondaryContainer = Color(0xFFF8DFBD),
    tertiary = Color(0xFFFFB4A2),
    onTertiary = Color(0xFF581D11),
    tertiaryContainer = Color(0xFF753425),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = Color(0xFF101413),
    onBackground = Color(0xFFE1E5E2),
    surface = Color(0xFF161B1A),
    onSurface = Color(0xFFE1E5E2),
    surfaceVariant = Color(0xFF262E2C),
    onSurfaceVariant = Color(0xFFC2C9C5),
    surfaceContainer = Color(0xFF1C2221),
    surfaceContainerHigh = Color(0xFF222927),
    outline = Color(0xFF8C9390),
    outlineVariant = Color(0xFF424946),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun AuralisThemeProvider(
    preferencesManager: ThemePreferencesManager,
    content: @Composable () -> Unit
) {
    val themeMode by preferencesManager.themeModeFlow.collectAsState()
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val controller = remember(themeMode, isDark, preferencesManager) {
        object : ThemeController {
            override val themeMode: AppThemeMode = themeMode
            override val isDark: Boolean = isDark

            override fun setThemeMode(mode: AppThemeMode) {
                preferencesManager.setThemeMode(mode)
            }

            override fun toggleNextTheme() {
                val nextMode = when (themeMode) {
                    AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                    AppThemeMode.LIGHT -> AppThemeMode.DARK
                    AppThemeMode.DARK -> AppThemeMode.SYSTEM
                }
                preferencesManager.setThemeMode(nextMode)
            }
        }
    }

    val colors = if (isDark) AuralisDarkColorScheme else AuralisLightColorScheme

    CompositionLocalProvider(LocalThemeController provides controller) {
        MaterialTheme(colorScheme = colors) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                content()
            }
        }
    }
}
