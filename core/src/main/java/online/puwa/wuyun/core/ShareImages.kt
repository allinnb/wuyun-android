package online.puwa.wuyun.core

/** 分享图规格：1080×1440 竖版 PNG 的全部内容与已换行文本。布局逻辑在纯 JVM 的 :core，可单测。 */
data class ShareImageSpec(
    val width: Int,
    val height: Int,
    val title: String,
    val share: String,
    val headline: String,
    val footer: String,
    val url: String,
    val accentHex: String,
    val titleLines: List<String>,
    val shareLines: List<String>,
    val headlineLines: List<String>,
)

/** 平台渲染器：:core 测试用 AWT 实现，app 用 android.graphics.Canvas 实现。 */
fun interface ShareImageRenderer {
    fun renderPng(spec: ShareImageSpec): ByteArray
}

object ShareImages {

    const val WIDTH = 1080
    const val HEIGHT = 1440
    const val MARGIN = 96

    /**
     * 按估算字宽换行（CJK 全角 ≈ fontSize，ASCII ≈ 0.55×fontSize），只换行不截字。
     */
    fun wrap(text: String, fontSize: Int, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        val cur = StringBuilder()
        var curW = 0.0
        fun charWidth(ch: Char): Double =
            if (ch.code in 0x2E80..0x9FFF || ch.code in 0xF900..0xFAFF ||
                ch.code in 0xFF00..0xFFEF || ch.code in 0x3000..0x303F
            ) fontSize.toDouble() else fontSize * 0.55
        for (ch in text) {
            if (ch == '\n') {
                lines.add(cur.toString())
                cur.clear()
                curW = 0.0
                continue
            }
            val w = charWidth(ch)
            if (cur.isNotEmpty() && curW + w > maxWidth) {
                lines.add(cur.toString())
                cur.clear()
                curW = 0.0
            }
            cur.append(ch)
            curW += w
        }
        lines.add(cur.toString())
        return lines
    }

    fun build(
        title: String,
        share: String,
        headline: String,
        footer: String,
        url: String,
        accentHex: String,
    ): ShareImageSpec {
        val maxW = WIDTH - MARGIN * 2
        return ShareImageSpec(
            width = WIDTH,
            height = HEIGHT,
            title = title,
            share = share,
            headline = headline,
            footer = footer,
            url = url,
            accentHex = accentHex,
            titleLines = wrap(title, 72, maxW),
            shareLines = wrap(share, 44, maxW),
            headlineLines = wrap(headline, 36, maxW),
        )
    }

    fun fromState(state: TodayState, footer: String, url: String, accentHex: String): ShareImageSpec =
        build(state.card.title, state.card.share, state.headline, footer, url, accentHex)
}
