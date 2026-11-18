package online.puwa.wuyun.core

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

/**
 * 验收测试：对 reference/golden.json 全量比对。
 * A. 1554 个 cases 逐字段一致；B. stepsOf(2026..2087) 一致；
 * C. 36 张卡可取且与推算一致；D. 2028-01-21 12:00 抽查。
 */
class GoldenTest {

    companion object {
        private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")
        private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val json = Json { ignoreUnknownKeys = true }

        private val golden: Golden by lazy {
            val res = checkNotNull(GoldenTest::class.java.classLoader.getResource("golden.json")) { "golden.json missing" }
            json.decodeFromString<Golden>(res.readText())
        }

        private val data: WuyunData by lazy { WuyunData.fromResources() }

        private fun instantOf(input: String): Instant =
            LocalDateTime.parse(input, FMT).atZone(SHANGHAI).toInstant()
    }

    @Test
    fun `A golden cases all fields match`() {
        val cases = golden.cases
        assertEquals("golden case count", 1554, cases.size)
        val failures = mutableListOf<String>()
        for (c in cases) {
            val r = WuyunCalc.compute(instantOf(c.input), data.yunqi)
            val branch = WuyunCalc.branchOf(r.ganzhi)
            val actual = listOf(
                "jieqi" to r.jieqi,
                "jieqiTime" to r.jieqiTime,
                "jieqiDay" to r.jieqiDay.toString(),
                "yunqiYear" to r.yunqiYear.toString(),
                "ganzhi" to r.ganzhi,
                "branch" to branch,
                "suiyun" to r.suiyun,
                "sitian" to r.sitian,
                "zaiquan" to r.zaiquan,
                "step" to r.step.toString(),
                "stepName" to r.stepName,
                "from" to r.from,
                "fromTime" to r.fromTime,
                "to" to r.to,
                "toTime" to r.toTime,
                "zhuqi" to r.zhuqi,
                "keqi" to r.keqi,
                "headline" to r.headline,
            )
            val expected = listOf(
                "jieqi" to c.jieqi,
                "jieqiTime" to c.jieqiTime,
                "jieqiDay" to c.jieqiDay.toString(),
                "yunqiYear" to c.yunqiYear.toString(),
                "ganzhi" to c.ganzhi,
                "branch" to c.branch,
                "suiyun" to c.suiyun,
                "sitian" to c.sitian,
                "zaiquan" to c.zaiquan,
                "step" to c.step.toString(),
                "stepName" to c.stepName,
                "from" to c.from,
                "fromTime" to c.fromTime,
                "to" to c.to,
                "toTime" to c.toTime,
                "zhuqi" to c.zhuqi,
                "keqi" to c.keqi,
                "headline" to c.headline,
            )
            for ((k, exp) in expected) {
                val act = actual.first { it.first == k }.second
                if (exp != act) failures += "input=${c.input} field=$k expected=<$exp> actual=<$act>"
            }
        }
        if (failures.isNotEmpty()) {
            fail("A: ${failures.size} field mismatches / ${cases.size} cases (showing up to 50):\n" +
                failures.take(50).joinToString("\n"))
        }
    }

    @Test
    fun `B stepsOf 2026-2087 matches golden`() {
        val failures = mutableListOf<String>()
        for (y in 2026..2087) {
            val expected = golden.stepsOf[y.toString()] ?: run {
                failures += "year=$y missing in golden.stepsOf"
                continue
            }
            val actual = WuyunCalc.stepsOf(y)
            if (actual.size != expected.size) {
                failures += "year=$y step count expected=${expected.size} actual=${actual.size}"
                continue
            }
            for (i in expected.indices) {
                val e = expected[i]
                val a = actual[i]
                for ((k, exp, act) in listOf(
                    Triple("step", e.step.toString(), a.step.toString()),
                    Triple("stepName", e.stepName, a.stepName),
                    Triple("from", e.from, a.from),
                    Triple("fromTime", e.fromTime, a.fromTime),
                    Triple("to", e.to, a.to),
                    Triple("toTime", e.toTime, a.toTime),
                )) {
                    if (exp != act) failures += "year=$y step=${e.step} field=$k expected=<$exp> actual=<$act>"
                }
            }
        }
        if (failures.isNotEmpty()) {
            fail("B: ${failures.size} mismatches (showing up to 50):\n" + failures.take(50).joinToString("\n"))
        }
    }

    @Test
    fun `C1 all 36 branch x step combos resolve to a card`() {
        val branches = listOf("子午", "丑未", "寅申", "卯酉", "辰戌", "巳亥")
        for (b in branches) {
            for (s in 1..6) {
                assertNotNull("cardFor($b, $s) returned null", data.cardFor(b, s))
            }
        }
        assertEquals("total cards", 36, data.allCards.size)
    }

    @Test
    fun `C2 card zhuqi keqi match compute for every golden case`() {
        val failures = mutableListOf<String>()
        for (c in golden.cases) {
            val r = WuyunCalc.compute(instantOf(c.input), data.yunqi)
            val branch = WuyunCalc.branchOf(r.ganzhi)
            val card = branch?.let { data.cardFor(it, r.step) }
            if (card == null) {
                failures += "input=${c.input} no card for branch=$branch step=${r.step}"
                continue
            }
            if (card.zhuqi != c.zhuqi || card.keqi != c.keqi) {
                failures += "input=${c.input} card($branch,${r.step}) zhuqi/keqi=<${card.zhuqi}/${card.keqi}> expected=<${c.zhuqi}/${c.keqi}>"
            }
            if (r.zhuqi != c.zhuqi || r.keqi != c.keqi) {
                failures += "input=${c.input} compute zhuqi/keqi=<${r.zhuqi}/${r.keqi}> expected=<${c.zhuqi}/${c.keqi}>"
            }
        }
        if (failures.isNotEmpty()) {
            fail("C: ${failures.size} mismatches (showing up to 50):\n" + failures.take(50).joinToString("\n"))
        }
    }

    @Test
    fun `D spot check 2028-01-21 12-00`() {
        val r = WuyunCalc.compute(instantOf("2028-01-21 12:00:00"), data.yunqi)
        assertEquals("大寒 · 第 2 天｜初之气（大寒—春分），戊申", r.headline)
        assertEquals("厥阴风木", r.zhuqi)
        assertEquals("少阴君火", r.keqi)
        assertEquals("大寒", r.jieqi)
        assertEquals(1, r.step)
        assertEquals("初之气", r.stepName)
        assertEquals(2028, r.yunqiYear)
        assertEquals("戊申", r.ganzhi)
        assertEquals("寅申", WuyunCalc.branchOf(r.ganzhi))
    }

    @Test
    fun `parseDateParam date only and datetime`() {
        val d = WuyunCalc.parseDateParam("2028-01-21")
        assertNotNull(d)
        assertEquals(LocalDateTime.of(2028, 1, 21, 12, 0).atZone(SHANGHAI).toInstant(), d)

        val t = WuyunCalc.parseDateParam("2028-01-21T08:30")
        assertNotNull(t)
        assertEquals(LocalDateTime.of(2028, 1, 21, 8, 30).atZone(SHANGHAI).toInstant(), t)

        // 00:30 北京时间 = 前一天 16:30 UTC（与 JS 版 Date.UTC 负小时回绕一致）
        val e = WuyunCalc.parseDateParam("2028-01-21T00:30")
        assertNotNull(e)
        assertEquals(LocalDateTime.of(2028, 1, 20, 16, 30).atZone(ZoneOffset.UTC).toInstant(), e)

        assertNull(WuyunCalc.parseDateParam("2028/01/21"))
        assertNull(WuyunCalc.parseDateParam(""))
        assertNull(WuyunCalc.parseDateParam(null))
    }

    @Test
    fun `branchOf maps all twelve branches`() {
        val expect = mapOf(
            "甲子" to "子午", "丙午" to "子午", "戊寅" to "寅申", "庚申" to "寅申",
            "壬辰" to "辰戌", "甲戌" to "辰戌", "乙丑" to "丑未", "丁未" to "丑未",
            "己卯" to "卯酉", "辛酉" to "卯酉", "癸巳" to "巳亥", "乙亥" to "巳亥",
        )
        for ((g, b) in expect) assertEquals(b, WuyunCalc.branchOf(g))
        assertNull(WuyunCalc.branchOf(null))
    }
}
