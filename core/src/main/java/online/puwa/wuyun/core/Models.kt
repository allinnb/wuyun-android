package online.puwa.wuyun.core

import kotlinx.serialization.Serializable

/** yunqi-60.json 中某一步的运气（主气/客气）。字段与 JSON 一一对应，原样保留。 */
@Serializable
data class YunqiStep(
    val step: Int,
    val from: String,
    val to: String,
    val zhuqi: String,
    val keqi: String,
    val same: Boolean,
)

/** yunqi-60.json 中一年（一个甲子序号）的运气。 */
@Serializable
data class YunqiYear(
    val index: Int,
    val ganzhi: String,
    val suiyun: String,
    val sitian: String,
    val zaiquan: String,
    val relation: String,
    val tags: List<String>,
    val zhuyun: List<String>,
    val keyun: List<String>,
    val steps: List<YunqiStep>,
)

/** assets/yunqi-60.json 整体结构。 */
@Serializable
data class Yunqi60(
    val note: String,
    val years: List<YunqiYear>,
)

/** assets/cards.json 中一张卡。字段一个不少、原样保留，文案只来自 JSON。 */
@Serializable
data class Card(
    val branch: String,
    val step: Int,
    val zhuqi: String,
    val keqi: String,
    val title: String,
    val sky: String,
    val body: String,
    val drink: String,
    val press: String,
    val move: String,
    val classic: String,
    val plain: String,
    val share: String,
    val classic_source: String,
    val classic_verified: String,
    val classic_note: String,
    val range: String,
    val sitian: String,
)

/** assets/cards.json 整体结构。 */
@Serializable
data class CardsFile(
    val style: String,
    val key: String,
    val cards: List<Card>,
)

/** compute() 的返回结果。字段名与 wuyun-calc.js 保持一致。 */
data class ComputeResult(
    val jieqi: String,
    val jieqiTime: String,
    val jieqiDay: Int,
    val yunqiYear: Int,
    val ganzhi: String?,
    val suiyun: String?,
    val sitian: String?,
    val zaiquan: String?,
    val step: Int,
    val stepName: String,
    val from: String,
    val fromTime: String,
    val to: String,
    val toTime: String,
    val zhuqi: String?,
    val keqi: String?,
    val headline: String,
)

/** stepsOf() 中一步的起止交节时刻。 */
data class StepSpan(
    val step: Int,
    val stepName: String,
    val from: String,
    val fromTime: String,
    val to: String,
    val toTime: String,
)
