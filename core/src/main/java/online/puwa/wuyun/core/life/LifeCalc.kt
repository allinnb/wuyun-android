package online.puwa.wuyun.core.life

import com.nlf.calendar.Solar
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import online.puwa.wuyun.core.WuyunCalc

private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")
private val YMDHMS: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

private fun parse(s: String): Instant = LocalDateTime.parse(s, YMDHMS).atZone(SHANGHAI).toInstant()
private fun fmt(i: Instant): String = YMDHMS.format(i.atZone(SHANGHAI))

// ======================= P2 四季作息 =======================

data class SeasonState(
    val season: Season,
    /** 本季起点（四立交节时刻，北京时间 yyyy-MM-dd HH:mm:ss） */
    val fromTime: String,
    /** 下一季起点 */
    val toTime: String,
    /** 本季第几天（交节当天为第 1 天） */
    val dayNo: Int,
    val next: Season,
)

object SeasonCalc {
    /** 四立的时刻：取 lunar-java 节气表，节气名来自 seasons.json 的 from 字段。 */
    private fun boundaries(year: Int, seasons: List<Season>): List<Pair<Instant, Int>> {
        val out = ArrayList<Pair<Instant, Int>>()
        for (y in (year - 1)..(year + 1)) {
            val tb = Solar.fromYmd(y, 6, 1).lunar.jieQiTable
            seasons.forEachIndexed { i, s ->
                tb[s.from]?.let { out += parse(it.toYmdHms()) to i }
            }
        }
        return out.distinctBy { it.first }.sortedBy { it.first }
    }

    fun of(instant: Instant, data: SeasonsFile): SeasonState {
        val p = instant.atZone(SHANGHAI)
        val b = boundaries(p.year, data.seasons)
        val idx = b.indexOfLast { !it.first.isAfter(instant) }
        check(idx >= 0 && idx + 1 < b.size) { "season boundary not found for $instant" }
        val (from, si) = b[idx]
        val (to, ni) = b[idx + 1]
        val day = ChronoUnit.DAYS.between(from.atZone(SHANGHAI).toLocalDate(), p.toLocalDate()).toInt() + 1
        return SeasonState(data.seasons[si], fmt(from), fmt(to), day, data.seasons[ni])
    }
}

// ======================= P2 十二时辰 =======================

data class ShichenState(val item: Shichen, val index: Int, val minutesLeft: Int)

object ShichenCalc {
    fun of(instant: Instant, data: ShichenFile): ShichenState {
        val t = instant.atZone(SHANGHAI).toLocalTime()
        val idx = ((t.hour + 1) / 2) % 12
        val item = data.items[idx]
        val endHour = (item.start + 2) % 24
        var left = ChronoUnit.MINUTES.between(t.withSecond(0).withNano(0), LocalTime.of(endHour, 0)).toInt()
        if (left <= 0) left += 24 * 60
        return ShichenState(item, idx, left)
    }
}

// ======================= P2 交节提醒 =======================

data class Reminder(
    val step: Int,
    val stepName: String,
    /** 交节节气名（新一步的起点） */
    val boundary: String,
    val boundaryTime: String,
    /** 提醒时刻：交节前 3 天的 09:00（北京时间） */
    val remindAt: String,
)

object ReminderPlanner {
    const val DAYS_BEFORE = 3L
    val REMIND_TIME: LocalTime = LocalTime.of(9, 0)

    /** 从 now 起之后的 count 个六气交节提醒（只返回 remindAt 晚于 now 的）。 */
    fun next(now: Instant, count: Int = 6): List<Reminder> {
        val y0 = now.atZone(SHANGHAI).year
        val out = ArrayList<Reminder>()
        for (y in (y0 - 1)..(y0 + 2)) {
            for (s in WuyunCalc.stepsOf(y)) {
                val bt = parse(s.fromTime)
                val day: LocalDate = bt.atZone(SHANGHAI).toLocalDate().minusDays(DAYS_BEFORE)
                val at = day.atTime(REMIND_TIME).atZone(SHANGHAI).toInstant()
                if (at.isAfter(now)) out += Reminder(s.step, s.stepName, s.from, s.fromTime, fmt(at))
            }
        }
        return out.distinctBy { it.boundaryTime }.sortedBy { it.boundaryTime }.take(count)
    }
}

// ======================= P3 生命节律 =======================

data class LifeState(
    val track: LifeTrack,
    val age: Int,
    /** 当前所处节点；还没到第一个节点时为 null */
    val stage: LifeStage?,
    /** 下一个节点；已过最后一个节点时为 null */
    val next: LifeStage?,
    val yearsToNext: Int?,
)

object LifecycleCalc {
    /** age 为周岁（按出生年份与当年相减的简化算法，界面上让用户直接选出生年）。 */
    fun of(birthYear: Int, trackId: String, nowYear: Int, data: LifecycleFile): LifeState {
        val track = data.tracks.first { it.id == trackId }
        val age = (nowYear - birthYear).coerceAtLeast(0)
        val stage = track.stages.lastOrNull { age >= it.age }
        val next = track.stages.firstOrNull { age < it.age }
        return LifeState(track, age, stage, next, next?.let { it.age - age })
    }
}

// ======================= P3 五态人测试 =======================

data class WutaiResult(val type: WutaiType, val scores: Map<String, Int>)

object WutaiQuiz {
    /** 平分时的优先次序：阴阳和平优先，其余按 JSON 中 types 的顺序。 */
    fun score(answers: List<Int>, data: WutaiFile): WutaiResult {
        require(answers.size == data.questions.size) { "answers=${answers.size} questions=${data.questions.size}" }
        val scores = LinkedHashMap<String, Int>()
        data.types.forEach { scores[it.id] = 0 }
        answers.forEachIndexed { qi, oi ->
            val t = data.questions[qi].options[oi].type
            scores[t] = (scores[t] ?: 0) + 1
        }
        val max = scores.values.max()
        val order = data.types.sortedBy { if (it.id == BALANCED) 0 else 1 }
        val winner = order.first { scores[it.id] == max }
        return WutaiResult(winner, scores)
    }

    const val BALANCED = "he"
}

// ======================= P4 五味餐盘 =======================

data class PlateSummary(val counts: Map<String, Int>, val line: String)

object PlateScorer {
    /** foods：今天点过的食物名（可重复）。规则：最多的味 ≥ 3 且占一半以上算偏多；否则报缺的味；全有算匀。 */
    fun summary(foods: List<String>, data: WuweiFile): PlateSummary {
        val byName = data.foods.associateBy { it.name }
        val counts = LinkedHashMap<String, Int>()
        data.flavors.forEach { counts[it.id] = 0 }
        foods.mapNotNull { byName[it] }.forEach { counts[it.flavor] = counts.getValue(it.flavor) + 1 }
        val total = counts.values.sum()
        val l = data.lines
        if (total == 0) return PlateSummary(counts, l.empty)
        val top = counts.maxBy { it.value }
        val name = { id: String -> data.flavors.first { it.id == id }.name }
        if (top.value >= 3 && top.value * 2 > total) {
            return PlateSummary(counts, l.heavy.replace("{name}", name(top.key)))
        }
        val missing = counts.filter { it.value == 0 }.keys.firstOrNull()
            ?: return PlateSummary(counts, l.even)
        val ex = data.foods.filter { it.flavor == missing }.take(2).joinToString(SEP) { it.name }
        return PlateSummary(counts, l.missing.replace("{name}", name(missing)).replace("{examples}", ex))
    }

    private const val SEP = "\u3001"
}

// ======================= P4 情志日记 =======================

/** 日记一条。level 为 1..3（对应 emotions.json 的 levels）。存 Room，由 :app 负责。 */
data class DiaryEntry(val date: LocalDate, val emotion: String, val level: Int, val note: String = "")

data class DiaryWeek(val counts: Map<String, Int>, val line: String, val care: Boolean)

object DiaryStats {
    const val POSITIVE = "xi"

    /** 统计 today 往前 7 天（含当天）。同一种非“喜”情绪 level≥2 出现在 ≥3 个不同日子时给出关心提示。 */
    fun weekly(entries: List<DiaryEntry>, today: LocalDate, data: EmotionsFile): DiaryWeek {
        val from = today.minusDays(6)
        val week = entries.filter { !it.date.isBefore(from) && !it.date.isAfter(today) }
        val counts = LinkedHashMap<String, Int>()
        data.emotions.forEach { counts[it.id] = 0 }
        week.forEach { counts[it.emotion] = (counts[it.emotion] ?: 0) + 1 }
        val l = data.lines
        val word = { id: String -> data.emotions.first { it.id == id }.word }
        if (week.isEmpty()) return DiaryWeek(counts, l.empty, false)
        val heavy = week.filter { it.emotion != POSITIVE && it.level >= 2 }
            .groupBy { it.emotion }.mapValues { e -> e.value.map { it.date }.distinct().size }
            .filter { it.value >= 3 }.maxByOrNull { it.value }
        if (heavy != null) return DiaryWeek(counts, l.care.replace("{word}", word(heavy.key)), true)
        val top = counts.maxBy { it.value }
        val strong = week.any { it.level >= 2 && it.emotion != POSITIVE }
        if (!strong && top.key == POSITIVE || week.all { it.level == 1 }) {
            return DiaryWeek(counts, l.calm, false)
        }
        return DiaryWeek(counts, l.top.replace("{word}", word(top.key)).replace("{count}", top.value.toString()), false)
    }
}
