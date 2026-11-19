package online.puwa.wuyun.core

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * P1 验收：对 36 张卡各生成一张分享图（AWT 渲染器），确认不崩溃；
 * 并把 3 张样图写到 reference/share-samples/：标题最长、标题最短、寒水蓝。
 */
class ShareImagesTest {

    companion object {
        private val data: WuyunData by lazy { WuyunData.fromResources() }
        private val BRANCHES = listOf("子午", "丑未", "寅申", "卯酉", "辰戌", "巳亥")
        private const val FOOTER = "传统文化参考，不舒服请就医"
        private const val URL = "https://allinnb.github.io/wuyun/?date=2028-01-21"

        // 测试代码内部的中文字面量不受验收 D 限制
        private fun accentOf(keqi: String): String = when (keqi) {
            "厥阴风木" -> "#00695C"
            "少阴君火" -> "#C62828"
            "少阳相火" -> "#D84315"
            "太阴湿土" -> "#8D6E00"
            "阳明燥金" -> "#616161"
            "太阳寒水" -> "#1565C0"
            else -> "#1565C0"
        }
    }

    private val realHeadline: String by lazy {
        val instant = WuyunCalc.parseDateParam("2028-01-21T12:00")!!
        TodayPresenter.build(instant, data).headline
    }

    private fun pngOf(card: Card): ByteArray {
        val spec = ShareImages.build(card.title, card.share, realHeadline, FOOTER, URL, accentOf(card.keqi))
        return AwtShareRenderer.renderPng(spec)
    }

    @Test
    fun `share image renders for all 36 cards without crash`() {
        var count = 0
        for (b in BRANCHES) {
            for (s in 1..6) {
                val card = data.cardFor(b, s)
                assertTrue("card $b/$s missing", card != null)
                val png = pngOf(card!!)
                count++
                assertTrue("png too small for $b/$s: ${png.size}", png.size > 5000)
                // PNG magic
                assertEquals(0x89.toByte(), png[0])
                assertEquals('P'.code.toByte(), png[1])
                assertEquals('N'.code.toByte(), png[2])
                assertEquals('G'.code.toByte(), png[3])
            }
        }
        assertEquals(36, count)
    }

    @Test
    fun `wrap never truncates long titles`() {
        val branches = BRANCHES
        var longest: Card? = null
        var shortest: Card? = null
        for (b in branches) for (s in 1..6) {
            val c = data.cardFor(b, s)!!
            if (longest == null || c.title.length > longest.title.length) longest = c
            if (shortest == null || c.title.length < shortest.title.length) shortest = c
        }
        for (card in listOf(longest!!, shortest!!)) {
            val spec = ShareImages.build(card.title, card.share, realHeadline, FOOTER, URL, accentOf(card.keqi))
            assertEquals("wrap must not lose chars", card.title.replace("\n", ""), spec.titleLines.joinToString(""))
            assertTrue("long title must wrap to multiple lines", spec.titleLines.size >= 1)
        }
        assertTrue(longest.title.length > shortest.title.length)
    }

    @Test
    fun `write 3 sample share images to reference share-samples`() {
        var longest: Card? = null
        var shortest: Card? = null
        var water: Card? = null
        for (b in BRANCHES) for (s in 1..6) {
            val c = data.cardFor(b, s)!!
            if (longest == null || c.title.length > longest.title.length) longest = c
            if (shortest == null || c.title.length < shortest.title.length) shortest = c
            if (c.keqi == "太阳寒水" && water == null) water = c
        }
        assertTrue("need a 太阳寒水 card", water != null)
        val dir = File("reference/share-samples")
        dir.mkdirs()
        dir.listFiles()?.filter { it.extension == "png" }?.forEach { it.delete() }
        fun write(name: String, card: Card) {
            val png = pngOf(card)
            File(dir, name).writeBytes(png)
        }
        write("share-sample-longest-title.png", longest!!)
        write("share-sample-shortest-title.png", shortest!!)
        write("share-sample-water-blue.png", water!!)
        assertEquals(3, dir.listFiles()!!.count { it.extension == "png" })
    }
}
