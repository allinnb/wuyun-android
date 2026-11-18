package online.puwa.wuyun.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun WuyunTheme(
    token: String,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) QiPalettes.dark(token) else QiPalettes.light(token)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color(0xFF000000),
            background = Color(0xFF121212),
            onBackground = Color(0xFFEDEDED),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFEDEDED),
            surfaceVariant = palette.accentSoft,
            onSurfaceVariant = Color(0xFFCFCFCF),
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color(0xFFFFFFFF),
            background = Color(0xFFFAFAF7),
            onBackground = Color(0xFF1A1A1A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            surfaceVariant = palette.accentSoft,
            onSurfaceVariant = Color(0xFF5F5F5F),
        )
    }
    CompositionLocalProvider(LocalQiPalette provides palette) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
