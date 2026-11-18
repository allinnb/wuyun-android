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
 *
 * 固定中文文案（节气名、步名、地支组名、headline 拼接片段）在 resources/wuyun-text.json，
 * 本文件不含中文字面量。
 */
object WuyunCalc {

    private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")

    private val text: WuyunText get() = WuyunTexts.value
    private val STEP_NAMES: List<String> get() = text.stepNames
    private val BOUNDS: List<String> get() = text.bounds
    private val LABEL: Map<String, String> get() = text.label
    private val BRANCH: Map<String, String> get() = text.branch

    private val tableCache = HashMap<Int, Map<String, Solar>>()

    private fun table(y: Int): Map<String, Solar> {
        tableCache[y]?.let { return it }
        synchronized(tableCache) {
            tableCache[y]?.let { return it }
            val t: Map<String, Solar> = Solar.fromYmd(y, 6, 1).lunar.jieQiTable
            tableCache[y] = t
            return t
        }
    }

    /** 两个 Solar 之间相差的日历天数（等价于 JS 版 Date.UTC 换算后取整）。 */
    private fun dayNo(a: Solar, b: Solar): Long =
        LocalDate.of(b.year, b.month, b.day).toEpochDay() -
            LocalDate.of(a.year, a.month, a.day).toEpochDay()

    fun compute(instant: Instant, yunqi: Yunqi60? = null): ComputeResult {
        val t = text
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
        var tb = table(y)
        if (nowStr < tb[BOUNDS[0]]!!.toYmdHms()) {
            // 大寒交节前仍属上一运气年
            y -= 1
            tb = table(y)
        }
        var step = 0
        for (i in 0..5) {
            if (nowStr >= tb[BOUNDS[i]]!!.toYmdHms() && nowStr < tb[BOUNDS[i + 1]]!!.toYmdHms()) step = i + 1
        }
        val idx = ((y - 4) % 60 + 60) % 60
        val yr = yunqi?.years?.getOrNull(idx)
        val st = yr?.steps?.getOrNull(step - 1)
        val from = tb[BOUNDS[step - 1]]!!
        val to = tb[BOUNDS[step]]!!
        val h = t.headline
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
            stepName = STEP_NAMES[step - 1] + h.qiSuffix,
            from = LABEL[BOUNDS[step - 1]] ?: BOUNDS[step - 1],
            fromTime = from.toYmdHms(),
            to = LABEL[BOUNDS[step]] ?: BOUNDS[step],
            toTime = to.toYmdHms(),
            zhuqi = st?.zhuqi,
            keqi = st?.keqi,
            headline = prev.name + h.daySep + (dayNo(jq, now) + 1) + h.dayTail +
                STEP_NAMES[step - 1] + h.qiSuffix + h.open +
                (LABEL[BOUNDS[step - 1]] ?: BOUNDS[step - 1]) + h.dash +
                (LABEL[BOUNDS[step]] ?: BOUNDS[step]) + h.close +
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
        val tb = table(y)
        return (0..5).map { i ->
            StepSpan(
                step = i + 1,
                stepName = STEP_NAMES[i] + text.headline.qiSuffix,
                from = LABEL[BOUNDS[i]] ?: BOUNDS[i],
                fromTime = tb[BOUNDS[i]]!!.toYmdHms(),
                to = LABEL[BOUNDS[i + 1]] ?: BOUNDS[i + 1],
                toTime = tb[BOUNDS[i + 1]]!!.toYmdHms(),
            )
        }
    }

    fun branchOf(ganzhi: String?): String? =
        ganzhi?.let { BRANCH[it[1].toString()] }
}
