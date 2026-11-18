package online.puwa.wuyun.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 时间轴一格。 */
data class TimelineItem(
    val label: String,
    val title: String,
    val qi: String,
    val active: Boolean,
)

/**
 * 今日页状态。界面上要显示的所有字符串都在这里拼好，Compose 只负责画。
 * 固定中文格式串在 resources/wuyun-text.json。
 */
data class TodayState(
    val headline: String,
    val yearLabel: String,
    val currentStep: Int,
    val selectedStep: Int,
    val rangeLine: String,
    val card: Card,
    val timeline: List<TimelineItem>,
    val shareText: String,
)

object TodayPresenter {

    private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")
    private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    const val SHARE_URL = "https://allinnb.github.io/wuyun/?date="

    fun build(instant: Instant, data: WuyunData, selectedStep: Int? = null): TodayState {
        val t = WuyunTexts.value
        val r = WuyunCalc.compute(instant, data.yunqi)
        val sel = (selectedStep ?: r.step).coerceIn(1, 6)
        val branch = WuyunCalc.branchOf(r.ganzhi)
            ?: error("no branch for ganzhi=${r.ganzhi}")
        val spans = WuyunCalc.stepsOf(r.yunqiYear)
        val yr = data.yunqi.years[((r.yunqiYear - 4) % 60 + 60) % 60]

        // rangeLine：{from}—{to}（{fromTime 前10位} 至 {toTime 前10位}） · 主气 {zhuqi} · 客气 {keqi}
        // from/to/时刻取自 stepsOf(yunqiYear)，主客气取自 yunqi-60.json 该年该步
        val rg = t.range
        val span = spans[sel - 1]
        val st = yr.steps[sel - 1]
        val rangeLine = span.from + rg.dash + span.to + rg.open +
            span.fromTime.substring(0, 10) + rg.dateSep + span.toTime.substring(0, 10) + rg.close +
            rg.zhuqiSep + st.zhuqi + rg.keqiSep + st.keqi

        val card = data.cardFor(branch, sel)
            ?: error("no card for branch=$branch step=$sel")

        // timeline：label = {stepName} · {from}—{to}（{fromTime 的 MM-DD} 至 {toTime 的 MM-DD}）
        val tl = t.timeline
        val timeline = spans.mapIndexed { i, s ->
            val stepNo = i + 1
            val stepCard = data.cardFor(branch, stepNo)
                ?: error("no card for branch=$branch step=$stepNo")
            val stp = yr.steps[i]
            TimelineItem(
                label = s.stepName + tl.mid + s.from + tl.dash + s.to + tl.open +
                    s.fromTime.substring(5, 10) + tl.dateSep + s.toTime.substring(5, 10) + tl.close,
                title = stepCard.title,
                qi = stp.zhuqi + t.qiSep + stp.keqi,
                active = stepNo == sel,
            )
        }

        val dateStr = DATE_FMT.format(instant.atZone(SHANGHAI))
        val shareText = card.share + "\n" + SHARE_URL + dateStr

        return TodayState(
            headline = r.headline,
            yearLabel = r.ganzhi ?: "",
            currentStep = r.step,
            selectedStep = sel,
            rangeLine = rangeLine,
            card = card,
            timeline = timeline,
            shareText = shareText,
        )
    }
}
