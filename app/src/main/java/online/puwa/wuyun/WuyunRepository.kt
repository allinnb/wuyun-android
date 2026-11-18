package online.puwa.wuyun

import android.content.Context
import online.puwa.wuyun.core.Card
import online.puwa.wuyun.core.WuyunData

/**
 * 数据层入口（Android 侧）：从 assets 读取 yunqi-60.json / cards.json，
 * 交给纯 JVM 的 :core 模块解析。文案全部来自 JSON，代码不写死文案。
 */
class WuyunRepository(context: Context) {

    val data: WuyunData by lazy {
        WuyunData.fromStreams(
            context.assets.open("yunqi-60.json"),
            context.assets.open("cards.json"),
        )
    }

    fun cardFor(branch: String, step: Int): Card? = data.cardFor(branch, step)
}
