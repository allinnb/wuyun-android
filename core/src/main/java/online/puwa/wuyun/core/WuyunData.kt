package online.puwa.wuyun.core

import java.io.InputStream
import kotlinx.serialization.json.Json

/**
 * 数据层：用 kotlinx.serialization 解析 assets/yunqi-60.json 与 assets/cards.json。
 * 文案只来自 JSON 文件，代码不写死任何文案。
 */
class WuyunData private constructor(
    val yunqi: Yunqi60,
    private val cards: Map<Pair<String, Int>, Card>,
) {
    /** 按 地支组（子午/丑未/寅申/卯酉/辰戌/巳亥）+ 步（1-6）取卡。 */
    fun cardFor(branch: String, step: Int): Card? = cards[branch to step]

    val allCards: List<Card> get() = cards.values.toList()

    companion object {
        private val json = Json

        fun fromJson(yunqiJson: String, cardsJson: String): WuyunData {
            val yunqi = json.decodeFromString<Yunqi60>(yunqiJson)
            val cardsFile = json.decodeFromString<CardsFile>(cardsJson)
            return WuyunData(yunqi, cardsFile.cards.associateBy { it.branch to it.step })
        }

        fun fromStreams(yunqiIn: InputStream, cardsIn: InputStream): WuyunData =
            fromJson(
                yunqiIn.use { it.readBytes().toString(Charsets.UTF_8) },
                cardsIn.use { it.readBytes().toString(Charsets.UTF_8) },
            )

        /** 从 classpath 资源加载（单元测试用；Android 侧走 assets，见 app 模块）。 */
        fun fromResources(classLoader: ClassLoader = WuyunData::class.java.classLoader): WuyunData =
            fromStreams(
                checkNotNull(classLoader.getResourceAsStream("yunqi-60.json")) { "yunqi-60.json not on classpath" },
                checkNotNull(classLoader.getResourceAsStream("cards.json")) { "cards.json not on classpath" },
            )
    }
}
