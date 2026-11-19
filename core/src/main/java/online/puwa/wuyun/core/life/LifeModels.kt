package online.puwa.wuyun.core.life

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** P2–P4 内容模型。字段与 assets 下各 JSON 一一对应，文案只来自 JSON。 */

@Serializable data class Bi(val zh: String, val en: String)

// ---------- P2 四季作息（四气调神） ----------
@Serializable data class Season(
    val id: String, val from: String, val name: String, val word: String,
    val title: String, val sleep: String, val body: String,
    @SerialName("do") val todo: List<String>,
    val classic: String, val plain: String,
    @SerialName("classic_note") val classicNote: String = "",
)
@Serializable data class SeasonsFile(
    val version: Int, val source: String,
    @SerialName("source_note") val sourceNote: String = "",
    val seasons: List<Season>,
)

// ---------- P2 十二时辰 ----------
@Serializable data class Shichen(
    val branch: String, val start: Int, val range: String,
    val meridian: String, val title: String, val tip: String,
)
@Serializable data class ShichenFile(
    val version: Int, val classic: String,
    @SerialName("classic_source") val classicSource: String,
    val plain: String, val about: String, val items: List<Shichen>,
)

// ---------- P3 生命节律（上古天真） ----------
@Serializable data class LifeStage(val n: Int, val age: Int, val classic: String, val zh: String, val en: String)
@Serializable data class LifeTrack(val id: String, val unit: Int, val label: Bi, val classic: String, val stages: List<LifeStage>)
@Serializable data class LifeClosing(val classic: String, val zh: String, val en: String)
@Serializable data class LifecycleFile(
    val version: Int, val source: String,
    @SerialName("source_note") val sourceNote: String = "",
    val about: Bi, val closing: LifeClosing, val tracks: List<LifeTrack>,
)

// ---------- P3 五态人（灵枢·通天） ----------
@Serializable data class WutaiType(
    val id: String, val name: Bi, val title: Bi, val plain: Bi, val classic: String,
    @SerialName("classic_note") val classicNote: String = "",
)
@Serializable data class WutaiOption(val t: Bi, val type: String)
@Serializable data class WutaiQuestion(val q: Bi, val options: List<WutaiOption>)
@Serializable data class WutaiFile(
    val version: Int, val source: String,
    @SerialName("source_note") val sourceNote: String = "",
    val about: Bi, val types: List<WutaiType>, val questions: List<WutaiQuestion>,
)

// ---------- P4 五味餐盘 ----------
@Serializable data class Flavor(val id: String, val name: String, val organ: String, val season: String)
@Serializable data class Food(val name: String, val flavor: String)
@Serializable data class PlateLines(val empty: String, val even: String, val heavy: String, val missing: String)
@Serializable data class WuweiFile(
    val version: Int, val classic: String,
    @SerialName("classic_source") val classicSource: String,
    val plain: String, val flavors: List<Flavor>, val foods: List<Food>, val lines: PlateLines,
)

// ---------- P4 情志日记 ----------
@Serializable data class Emotion(val id: String, val name: String, val word: String, val organ: String, val line: String)
@Serializable data class DiaryLines(val empty: String, val top: String, val calm: String, val care: String)
@Serializable data class EmotionsFile(
    val version: Int, val classic: String,
    @SerialName("classic_source") val classicSource: String,
    val classic2: String,
    @SerialName("classic2_source") val classic2Source: String,
    val plain: String, val privacy: String,
    val emotions: List<Emotion>, val levels: List<String>, val lines: DiaryLines,
)

// ---------- P4 五音 ----------
@Serializable data class Tone(val id: String, val name: String, val organ: String, val season: String, val classic: String, val mood: String)
@Serializable data class Track(val tone: String, val title: String, val artist: String, val license: String, val asset: String, val sourceUrl: String)
@Serializable data class WuyinFile(
    val version: Int,
    @SerialName("classic_source") val classicSource: String,
    val about: String, val tones: List<Tone>, val tracks: List<Track> = emptyList(),
)
