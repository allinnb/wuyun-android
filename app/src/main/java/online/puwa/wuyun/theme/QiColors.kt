package online.puwa.wuyun.theme

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.serialization.json.Json

/**
 * 主色：按选中步的客气取色。客气名到色名的映射在 assets/qi-colors.json（中文键不进 Kotlin 源码）。
 * 浅色/深色各一套强调色；强调色用于标题与描边，背景只用淡色，保证文字对比度 >= 4.5:1。
 */
data class QiPalette(val accent: Color, val accentSoft: Color, val accentHex: String)

object QiPalettes {

    fun loadTokens(context: Context): Map<String, String> {
        val json = context.assets.open("qi-colors.json").use { it.readBytes().toString(Charsets.UTF_8) }
        return Json.decodeFromString<Map<String, String>>(json)
    }

    fun hex(token: String, dark: Boolean): String = when (token) {
        "qing" -> if (dark) "#4DB6AC" else "#00695C"
        "hong" -> if (dark) "#EF5350" else "#C62828"
        "cheng" -> if (dark) "#FF8A65" else "#BF360C"
        "huang" -> if (dark) "#FFD54F" else "#8D6E00"
        "bai" -> if (dark) "#E0E0E0" else "#616161"
        "lan" -> if (dark) "#64B5F6" else "#1565C0"
        else -> if (dark) "#64B5F6" else "#1565C0"
    }

    fun light(token: String): QiPalette {
        val hex = hex(token, dark = false)
        val accent = Color(hex.toColorInt())
        return QiPalette(accent, accent.copy(alpha = 0.10f), hex)
    }

    fun dark(token: String): QiPalette {
        val hex = hex(token, dark = true)
        val accent = Color(hex.toColorInt())
        return QiPalette(accent, accent.copy(alpha = 0.16f), hex)
    }
}

val LocalQiPalette = staticCompositionLocalOf { QiPalettes.light("lan") }
