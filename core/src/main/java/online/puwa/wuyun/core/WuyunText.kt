package online.puwa.wuyun.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 固定的中文文案/格式串。界面与推算用到的固定文字统一放在 resources/wuyun-text.json，
 * Kotlin 源码里不出现中文字面量（验收 D）。卡片正文文案仍只来自 assets 的 JSON。
 */

@Serializable
data class HeadlineText(
    val daySep: String,
    val dayTail: String,
    val qiSuffix: String,
    val open: String,
    val dash: String,
    val close: String,
)

@Serializable
data class RangeText(
    val dash: String,
    val open: String,
    val dateSep: String,
    val close: String,
    val zhuqiSep: String,
    val keqiSep: String,
)

@Serializable
data class TimelineText(
    val mid: String,
    val dash: String,
    val open: String,
    val dateSep: String,
    val close: String,
)

@Serializable
data class WuyunText(
    val stepNames: List<String>,
    val bounds: List<String>,
    val label: Map<String, String>,
    val branch: Map<String, String>,
    val headline: HeadlineText,
    val range: RangeText,
    val timeline: TimelineText,
    val qiSep: String,
)

object WuyunTexts {
    val value: WuyunText by lazy {
        val res = checkNotNull(WuyunTexts::class.java.getResourceAsStream("/wuyun-text.json")) {
            "wuyun-text.json not on classpath"
        }
        Json.decodeFromString<WuyunText>(res.readBytes().toString(Charsets.UTF_8))
    }
}
