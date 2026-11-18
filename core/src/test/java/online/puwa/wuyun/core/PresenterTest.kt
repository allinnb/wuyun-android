package online.puwa.wuyun.core

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * P1 验收：
 * B. TodayPresenter 表格逐字比对 card.title 与 rangeLine 的区间部分；
 * C. golden 全部 input × selectedStep(null,1..6) 扫描。
 */
class PresenterTest {

    companion object {
        private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")
        private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val json = Json { ignoreUnknownKeys = true }

        private val golden: Golden by lazy {
            val res = checkNotNull(PresenterTest::class.java.classLoader.getResource("golden.json"))
            json.decodeFromString<Golden>(res.readText())
        }

        val data: WuyunData by lazy { WuyunData.fromResources() }

        private fun instantOf(input: String): Instant =
            LocalDateTime.parse(input, FMT).atZone(SHANGHAI).toInstant()
    }

    private data class Row(
        val input: String,
        val sel: Int?,
        val title: String,
        val interval: String,
        val rangeTail: String? = null,
    )

    @Test
    fun `B presenter table matches`() {
        val rows = listOf(
            Row("2026-11-08T12:00", null, "干燥，还带点火", "秋分—小雪（2026-09-23 至 2026-11-22）"),
            Row("2026-05-01T12:00", null, "暖风到岗，偶尔倒春寒", "春分—小满（2026-03-20 至 2026-05-21）"),
            Row("2027-01-20T16:00", null, "新的一年，先刮风", "大寒—春分（2027-01-20 至 2027-03-21）"),
            Row("2028-01-21T12:00", null, "风大，暖得太早", "大寒—春分（2028-01-20 至 2028-03-20）"),
            Row(
                "2028-01-21T12:00", 3, "主客都是火，双倍热", "小满—大暑（2028-05-20 至 2028-07-22）",
                rangeTail = " · 主气 少阳相火 · 客气 少阳相火",
            ),
            Row("2031-06-01T12:00", null, "风大，头也晕", "小满—大暑（2031-05-21 至 2031-07-23）"),
        )
        val failures = mutableListOf<String>()
        for (row in rows) {
            val instant = WuyunCalc.parseDateParam(row.input)
            assertNotNull("parseDateParam(${row.input})", instant)
            val st = TodayPresenter.build(instant!!, data, row.sel)
            if (st.card.title != row.title) {
                failures += "input=${row.input} sel=${row.sel} title expected=<${row.title}> actual=<${st.card.title}>"
            }
            if (!st.rangeLine.startsWith(row.interval)) {
                failures += "input=${row.input} sel=${row.sel} rangeLine expected interval=<${row.interval}> actual=<${st.rangeLine}>"
            }
            if (row.rangeTail != null && !st.rangeLine.endsWith(row.rangeTail)) {
                failures += "input=${row.input} sel=${row.sel} rangeLine expected tail=<${row.rangeTail}> actual=<${st.rangeLine}>"
            }
            if (row.sel != null) {
                // 点选步不改变 headline
                val r = WuyunCalc.compute(instant, data.yunqi)
                if (st.headline != r.headline) {
                    failures += "input=${row.input} sel=${row.sel} headline changed: <${st.headline}> vs compute <${r.headline}>"
                }
                if (st.selectedStep != row.sel || st.currentStep != r.step) {
                    failures += "input=${row.input} sel=${row.sel} steps: selected=${st.selectedStep} current=${st.currentStep} compute=${r.step}"
                }
            }
        }
        if (failures.isNotEmpty()) fail("B: ${failures.size} mismatches:\n" + failures.joinToString("\n"))
    }

    @Test
    fun `C scan all golden inputs x selectedStep`() {
        val cases = golden.cases
        assertEquals(1554, cases.size)
        val banned = listOf("null", "undefined", "准备中", "待补")
        val failures = mutableListOf<String>()
        var checked = 0
        for (c in cases) {
            val instant = instantOf(c.input)
            for (sel in listOf<Int?>(null, 1, 2, 3, 4, 5, 6)) {
                val st = TodayPresenter.build(instant, data, sel)
                checked++
                val card = st.card
                val cardFields = mapOf(
                    "title" to card.title, "sky" to card.sky, "body" to card.body,
                    "drink" to card.drink, "press" to card.press, "move" to card.move,
                    "classic" to card.classic, "plain" to card.plain, "share" to card.share,
                )
                for ((k, v) in cardFields) {
                    if (v.isBlank()) failures += "input=${c.input} sel=$sel card.$k is empty"
                }
                if (st.timeline.size != 6) {
                    failures += "input=${c.input} sel=$sel timeline.size=${st.timeline.size}"
                }
                val allStrings = mutableListOf(st.headline, st.yearLabel, st.rangeLine, st.shareText)
                allStrings += cardFields.values
                for (item in st.timeline) {
                    if (item.title.isBlank()) failures += "input=${c.input} sel=$sel timeline.title empty (label=${item.label})"
                    allStrings += listOf(item.label, item.title, item.qi)
                }
                for (s in allStrings) {
                    for (b in banned) {
                        if (s.contains(b)) failures += "input=${c.input} sel=$sel contains \"$b\": <$s>"
                    }
                }
            }
        }
        if (failures.isNotEmpty()) {
            fail("C: $checked states checked, ${failures.size} problems (showing up to 50):\n" +
                failures.take(50).joinToString("\n"))
        }
    }
}
