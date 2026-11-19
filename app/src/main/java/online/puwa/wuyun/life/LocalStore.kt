package online.puwa.wuyun.life

import android.content.Context
import java.io.File
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import online.puwa.wuyun.core.life.DiaryEntry

/**
 * 本地存储（P4）：情志日记、餐盘、设置。只存在 filesDir，不上传、不申请权限。
 * 用 JSON 文件而不是 Room，免依赖；数据量（每天几条）足够。
 */
class LocalStore(context: Context) {
    private val dir = context.filesDir
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable private data class DiaryRow(val date: String, val emotion: String, val level: Int, val note: String = "")
    @Serializable private data class PlateDay(val date: String, val foods: List<String>)
    @Serializable data class Settings(
        val birthYear: Int? = null,
        val track: String? = null,
        val remind: Boolean = false,
        val lang: String = "zh",
    )

    private fun file(name: String) = File(dir, name)
    private inline fun <reified T> read(name: String, default: T): T =
        runCatching { json.decodeFromString<T>(file(name).readText()) }.getOrDefault(default)
    private inline fun <reified T> write(name: String, v: T) {
        val tmp = file("$name.tmp"); tmp.writeText(json.encodeToString(v)); tmp.renameTo(file(name))
    }

    // ---- 日记 ----
    fun diary(): List<DiaryEntry> = read<List<DiaryRow>>(DIARY, emptyList())
        .map { DiaryEntry(LocalDate.parse(it.date), it.emotion, it.level, it.note) }

    fun addDiary(e: DiaryEntry) {
        val rows = read<List<DiaryRow>>(DIARY, emptyList()) + DiaryRow(e.date.toString(), e.emotion, e.level, e.note)
        write(DIARY, rows)
    }

    fun clearDiary() { file(DIARY).delete() }

    // ---- 餐盘（只留最近 30 天） ----
    fun plate(day: LocalDate): List<String> =
        read<List<PlateDay>>(PLATE, emptyList()).firstOrNull { it.date == day.toString() }?.foods ?: emptyList()

    fun setPlate(day: LocalDate, foods: List<String>) {
        val keep = read<List<PlateDay>>(PLATE, emptyList())
            .filter { it.date != day.toString() && LocalDate.parse(it.date).isAfter(day.minusDays(30)) }
        write(PLATE, keep + PlateDay(day.toString(), foods))
    }

    // ---- 设置 ----
    fun settings(): Settings = read(SETTINGS, Settings())
    fun saveSettings(s: Settings) = write(SETTINGS, s)

    companion object {
        private const val DIARY = "diary.json"
        private const val PLATE = "plate.json"
        private const val SETTINGS = "settings.json"
    }
}
