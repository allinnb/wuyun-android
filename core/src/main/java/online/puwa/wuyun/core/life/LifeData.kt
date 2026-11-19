package online.puwa.wuyun.core.life

import kotlinx.serialization.json.Json

/** P2–P4 内容一次加载。Android 侧用 assets.open(name) 传入 reader。 */
class LifeData(
    val seasons: SeasonsFile,
    val shichen: ShichenFile,
    val lifecycle: LifecycleFile,
    val wutai: WutaiFile,
    val wuwei: WuweiFile,
    val emotions: EmotionsFile,
    val wuyin: WuyinFile,
) {
    companion object {
        private val json = Json

        fun load(read: (String) -> String): LifeData = LifeData(
            json.decodeFromString(read("seasons.json")),
            json.decodeFromString(read("shichen.json")),
            json.decodeFromString(read("lifecycle.json")),
            json.decodeFromString(read("wutai.json")),
            json.decodeFromString(read("wuwei.json")),
            json.decodeFromString(read("emotions.json")),
            json.decodeFromString(read("wuyin.json")),
        )

        fun fromResources(cl: ClassLoader = LifeData::class.java.classLoader): LifeData = load { name ->
            checkNotNull(cl.getResourceAsStream(name)) { "$name not on classpath" }
                .use { it.readBytes().toString(Charsets.UTF_8) }
        }
    }
}
