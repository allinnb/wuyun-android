package online.puwa.wuyun.core

import java.io.File
import java.time.Instant
import java.time.LocalDate
import online.puwa.wuyun.core.life.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** P2–P4 验收（Hark 编写）。 */
class LifeTest {
    private val d = LifeData.fromResources()
    private fun at(s: String): Instant = WuyunCalc.parseDateParam(s)!!

    // ---------- P2 四季 ----------
    @Test fun seasonTable() {
        val rows = listOf(
            "2026-11-19T09:00" to "winter", "2026-05-01T12:00" to "spring",
            "2026-05-10T12:00" to "summer", "2026-08-10T12:00" to "autumn",
            "2027-01-20T16:00" to "winter", "2027-02-10T12:00" to "spring",
        )
        for ((q, id) in rows) assertEquals(q, id, SeasonCalc.of(at(q), d.seasons).season.id)
    }

    @Test fun seasonScan() {
        var day = LocalDate.of(2026, 1, 1)
        var prev: String? = null
        var changes = 0
        val order = d.seasons.seasons.map { it.id }
        while (day.year <= 2087) {
            val s = SeasonCalc.of(at("$day"), d.seasons)
            assertTrue("$day dayNo=${s.dayNo}", s.dayNo in 1..100)
            assertTrue("$day", s.toTime > s.fromTime)
            assertEquals(order[(order.indexOf(s.season.id) + 1) % 4], s.next.id)
            if (prev != null && prev != s.season.id) {
                assertEquals("$day", order[(order.indexOf(prev) + 1) % 4], s.season.id); changes++
            }
            prev = s.season.id
            day = day.plusDays(1)
        }
        assertEquals(62 * 4, changes)
    }

    // ---------- P2 时辰 ----------
    @Test fun shichen() {
        val rows = listOf("2026-11-19T23:30" to "子", "2026-11-19T00:59" to "子", "2026-11-19T01:00" to "丑",
            "2026-11-19T12:00" to "午", "2026-11-19T22:59" to "亥", "2026-11-19T05:00" to "卯")
        for ((q, b) in rows) assertEquals(q, b, ShichenCalc.of(at(q), d.shichen).item.branch)
        assertEquals(120, ShichenCalc.of(at("2026-11-19T23:00"), d.shichen).minutesLeft)
        assertEquals(1, ShichenCalc.of(at("2026-11-19T00:59"), d.shichen).minutesLeft)
        for (h in 0..23) assertEquals(((h + 1) / 2) % 12, d.shichen.items.indexOfFirst {
            val e = (it.start + 2) % 24; if (it.start < e) h >= it.start && h < e else h >= it.start || h < e })
    }

    // ---------- P2 交节提醒 ----------
    @Test fun reminders() {
        val r = ReminderPlanner.next(at("2026-11-08T12:00"), 6)
        assertEquals(6, r.size)
        assertEquals("小雪", r[0].boundary)
        assertTrue(r[0].boundaryTime.startsWith("2026-11-22"))
        assertEquals("2026-11-19 09:00:00", r[0].remindAt)
        assertEquals(listOf("小雪", "大寒", "春分", "小满", "大暑", "秋分"), r.map { it.boundary })
        val r2 = ReminderPlanner.next(at("2026-11-19T09:30"), 1)
        assertEquals("大寒", r2[0].boundary)
    }

    // ---------- P3 生命节律 ----------
    @Test fun lifecycle() {
        val f = d.lifecycle
        assertEquals(5, LifecycleCalc.of(1991, "seven", 2026, f).stage!!.n)
        assertNull(LifecycleCalc.of(2020, "seven", 2026, f).stage)
        assertEquals(7, LifecycleCalc.of(1950, "seven", 2026, f).stage!!.n)
        assertNull(LifecycleCalc.of(1950, "seven", 2026, f).next)
        assertEquals(5, LifecycleCalc.of(1986, "eight", 2026, f).stage!!.n)
        assertEquals(7, LifecycleCalc.of(1963, "eight", 2026, f).stage!!.n)
        assertEquals(8, LifecycleCalc.of(1962, "eight", 2026, f).stage!!.n)
        assertEquals(7, LifecycleCalc.of(1991, "seven", 2026, f).yearsToNext)
        for (t in f.tracks) {
            t.stages.forEachIndexed { i, s -> assertEquals(t.unit * (i + 1), s.age); assertTrue(t.classic.contains(s.classic)) }
        }
    }

    // ---------- P3 五态人 ----------
    @Test fun wutai() {
        val w = d.wutai
        val ids = w.types.map { it.id }.toSet()
        assertEquals(5, ids.size)
        w.questions.forEach { q -> assertEquals(4, q.options.size); q.options.forEach { assertTrue(it.type in ids) } }
        val allHe = w.questions.map { q -> q.options.indexOfFirst { it.type == "he" } }
        assertEquals("he", WutaiQuiz.score(allHe, w).type.id)
        val allFirst = w.questions.map { 0 }
        val r = WutaiQuiz.score(allFirst, w)
        assertEquals(r.scores.values.max(), r.scores[r.type.id])
        // 每种类型都能被测出来
        for (id in ids) {
            val ans = w.questions.map { q -> q.options.indexOfFirst { it.type == id }.let { if (it < 0) q.options.indexOfFirst { o -> o.type == "he" }.let { h -> (0 until 4).first { k -> k != h } } else it } }
            assertEquals(id, WutaiQuiz.score(ans, w).type.id)
        }
    }

    // ---------- P4 餐盘 ----------
    @Test fun plate() {
        val w = d.wuwei
        assertEquals(w.lines.empty, PlateScorer.summary(emptyList(), w).line)
        assertEquals("今天辛味有点多，下一顿换换口。", PlateScorer.summary(listOf("生姜", "葱", "白萝卜"), w).line)
        assertEquals(w.lines.even, PlateScorer.summary(listOf("醋", "苦瓜", "米饭", "生姜", "海带"), w).line)
        assertEquals("还没碰到酸味，可以看看山楂、乌梅。", PlateScorer.summary(listOf("苦瓜", "米饭"), w).line)
        assertTrue(w.foods.all { f -> w.flavors.any { it.id == f.flavor } })
    }

    // ---------- P4 情志 ----------
    @Test fun diary() {
        val e = d.emotions
        val t = LocalDate.of(2026, 11, 19)
        assertEquals(e.lines.empty, DiaryStats.weekly(emptyList(), t, e).line)
        val sad = (0..2).map { DiaryEntry(t.minusDays(it.toLong()), "bei", 2) }
        val w = DiaryStats.weekly(sad, t, e)
        assertTrue(w.care); assertEquals("连着好几天都很难过，找个信得过的人聊聊吧，需要的话也可以找专业的人。", w.line)
        val mix = listOf(DiaryEntry(t, "nu", 2), DiaryEntry(t.minusDays(1), "nu", 3), DiaryEntry(t, "xi", 1))
        assertEquals("这周记得最多的是火大，一共2次。", DiaryStats.weekly(mix, t, e).line)
        assertEquals(e.lines.calm, DiaryStats.weekly(listOf(DiaryEntry(t, "xi", 3)), t, e).line)
        assertFalse(DiaryStats.weekly(listOf(DiaryEntry(t.minusDays(9), "bei", 3)), t, e).care)
    }

    // ---------- 内容规范 ----------
    @Test fun contentLint() {
        val banned = listOf("null", "undefined", "准备中", "待补", "…", "...", "治疗", "疗效", "治愈", "克")
        val assets = File("app/src/main/assets")
        for (n in listOf("seasons", "shichen", "lifecycle", "wutai", "wuwei", "emotions", "wuyin")) {
            val txt = File(assets, "$n.json").readText()
            for (b in banned) assertFalse("$n.json contains $b", txt.contains(b))
        }
        d.seasons.seasons.forEach { assertTrue(it.id + " body " + it.body.length, it.body.length <= 60) }
        // 四季时间轴与运气卡同一套：DSH/Mistral 不得改 JSON
        val src = File("core/src/main/java/online/puwa/wuyun/core/life").listFiles()!!
        val han = Regex("\"[^\"]*[\\u4e00-\\u9fff][^\"]*\"")
        src.forEach { f -> f.readLines().filterNot { it.trim().startsWith("//") || it.trim().startsWith("*") || it.trim().startsWith("/**") }
            .forEach { assertFalse(f.name + ": " + it, han.containsMatchIn(it)) } }
    }
}
