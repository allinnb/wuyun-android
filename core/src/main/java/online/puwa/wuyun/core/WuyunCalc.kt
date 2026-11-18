package online.puwa.wuyun.core

import com.nlf.calendar.JieQi
import com.nlf.calendar.Solar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * 五运六气推算。逐行移植自 reference/wuyun-calc.js（lunar-javascript 版），
 * 节气库使用同源的 lunar-java（cn.6tail:lunar:1.7.8）。
 * 所有时刻按北京时间（Asia/Shanghai）计算，不依赖设备时区。
 * 时间字符串格式为 "yyyy-MM-dd HH:mm:ss"。
 */
object WuyunCalc {

    private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")

    val STEP_NAMES = listOf("初", "二", "三", "四", "五", "终")
    val BOUNDS = listOf("大寒", "春分", "小满", "大暑", "秋分", "小雪", "DA_HAN")
    val LABEL = mapOf("DA_HAN" to "大寒")
    val BRANCH = mapOf(
        '子' to "子午", '午' to "子午",
        '丑' to "丑未", '未' to "丑未",
        '寅' to "寅申", '申' to "寅申",
        '卯' to "卯酉", '酉' to "卯酉",
        '辰' to "辰戌", '戌' to "辰戌",
        '巳' to "巳亥", '亥' to "巳亥",
    )

    private fun table(y: Int): Map<String, Solar> =
        Solar.fromYmd(y, 6, 1).lunar.jieQiTable

    /** 两个 Solar 之间相差的日历天数（等价于 JS 版 Date.UTC 换算后取整）。 */
    private fun dayNo(a: Solar, b: Solar): Long =
        LocalDate.of(b.year, b.month, b.day).toEpochDay() -
            LocalDate.of(a.year, a.month, a.day).toEpochDay()

    fun compute(instant: Instant, yunqi: Yunqi60? = null): ComputeResult {
        val p = instant.atZone(SHANGHAI)
        val now = Solar.fromYmdHms(p.year, p.monthValue, p.dayOfMonth, p.hour, p.minute, p.second)
        val nowStr = now.toYmdHms()
        var prev: JieQi = now.lunar.getPrevJieQi(true)
        if (prev.solar.toYmdHms() > nowStr) {
            // 交节当天、交节时刻之前仍算上一个节气
            prev = prev.solar.next(-1).lunar.getPrevJieQi(true)
        }
        val jq = prev.solar
        var y = p.year
        var t = table(y)
        if (nowStr < t["大寒"]!!.toYmdHms()) {
            // 大寒交节前仍属上一运气年
            y -= 1
            t = table(y)
        }
        var step = 0
        for (i in 0..5) {
            if (nowStr >= t[BOUNDS[i]]!!.toYmdHms() && nowStr < t[BOUNDS[i + 1]]!!.toYmdHms()) step = i + 1
        }
        val idx = ((y - 4) % 60 + 60) % 60
        val yr = yunqi?.years?.getOrNull(idx)
        val st = yr?.steps?.getOrNull(step - 1)
        val from = t[BOUNDS[step - 1]]!!
        val to = t[BOUNDS[step]]!!
        return ComputeResult(
            jieqi = prev.name,
            jieqiTime = jq.toYmdHms(),
            jieqiDay = (dayNo(jq, now) + 1).toInt(),
            yunqiYear = y,
            ganzhi = yr?.ganzhi,
            suiyun = yr?.suiyun,
            sitian = yr?.sitian,
            zaiquan = yr?.zaiquan,
            step = step,
            stepName = STEP_NAMES[step - 1] + "之气",
            from = LABEL[BOUNDS[step - 1]] ?: BOUNDS[step - 1],
            fromTime = from.toYmdHms(),
            to = LABEL[BOUNDS[step]] ?: BOUNDS[step],
            toTime = to.toYmdHms(),
            zhuqi = st?.zhuqi,
            keqi = st?.keqi,
            headline = prev.name + " · 第 " + (dayNo(jq, now) + 1) + " 天｜" +
                STEP_NAMES[step - 1] + "之气（" +
                (LABEL[BOUNDS[step - 1]] ?: BOUNDS[step - 1]) + "—" +
                (LABEL[BOUNDS[step]] ?: BOUNDS[step]) + "），" +
                (yr?.ganzhi ?: ""),
        )
    }

    /**
     * 解析 ?date= 参数："YYYY-MM-DD" 或 "YYYY-MM-DDTHH:mm"，按北京时间解析。
     * 只有日期时取当天 12:00。格式不符返回 null。
     */
    fun parseDateParam(q: String?): Instant? {
        val m = Regex("^(\\d{4})-(\\d{2})-(\\d{2})(?:T(\\d{2}):(\\d{2}))?$").find(q ?: "") ?: return null
        val y = m.groupValues[1].toInt()
        val mo = m.groupValues[2].toInt()
        val d = m.groupValues[3].toInt()
        val h = if (m.groupValues[4].isNotEmpty()) m.groupValues[4].toInt() else 12
        val mi = if (m.groupValues[5].isNotEmpty()) m.groupValues[5].toInt() else 0
        // 等价于 JS 版 Date.UTC(y, mo-1, d, h-8, mi)：北京时间 = UTC+8。
        // 注意 java.util.Date.UTC 的年份参数是 year-1900（与 JS 版不同），这里要先减 1900。
        @Suppress("DEPRECATION")
        return Instant.ofEpochMilli(Date.UTC(y - 1900, mo - 1, d, h - 8, mi, 0))
    }

    /** 某运气年六步的起止交节时刻（北京时间）。 */
    fun stepsOf(y: Int): List<StepSpan> {
        val t = table(y)
        return (0..5).map { i ->
            StepSpan(
                step = i + 1,
                stepName = STEP_NAMES[i] + "之气",
                from = LABEL[BOUNDS[i]] ?: BOUNDS[i],
                fromTime = t[BOUNDS[i]]!!.toYmdHms(),
                to = LABEL[BOUNDS[i + 1]] ?: BOUNDS[i + 1],
                toTime = t[BOUNDS[i + 1]]!!.toYmdHms(),
            )
        }
    }

    fun branchOf(ganzhi: String?): String? =
        ganzhi?.let { BRANCH[it[1]] }
}
