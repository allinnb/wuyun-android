package online.puwa.wuyun.core

import kotlinx.serialization.Serializable

/** reference/golden.json 的反序列化模型（仅测试使用）。 */

@Serializable
data class GoldenCase(
    val input: String,
    val jieqi: String,
    val jieqiTime: String,
    val jieqiDay: Int,
    val yunqiYear: Int,
    val ganzhi: String,
    val branch: String,
    val suiyun: String,
    val sitian: String,
    val zaiquan: String,
    val step: Int,
    val stepName: String,
    val from: String,
    val fromTime: String,
    val to: String,
    val toTime: String,
    val zhuqi: String,
    val keqi: String,
    val headline: String,
)

@Serializable
data class GoldenStep(
    val step: Int,
    val stepName: String,
    val from: String,
    val fromTime: String,
    val to: String,
    val toTime: String,
)

@Serializable
data class Golden(
    val generated: String,
    val source: String,
    val timezone: String,
    val cases: List<GoldenCase>,
    val stepsOf: Map<String, List<GoldenStep>>,
)
