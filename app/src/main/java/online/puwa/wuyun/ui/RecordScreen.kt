package online.puwa.wuyun.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import online.puwa.wuyun.R
import online.puwa.wuyun.core.life.DiaryEntry
import online.puwa.wuyun.core.life.DiaryStats
import online.puwa.wuyun.core.life.LifeData
import online.puwa.wuyun.core.life.PlateScorer
import online.puwa.wuyun.life.LocalStore

/** P4 记录：五味餐盘、情志日记、五音。全部本地。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordScreen(now: Instant, life: LifeData, store: LocalStore, modifier: Modifier = Modifier) {
    val today = now.atZone(ZoneId.of("Asia/Shanghai")).toLocalDate()
    var tab by remember { mutableStateOf(0) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text(stringResource(R.string.ui_plate)) })
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text(stringResource(R.string.ui_diary)) })
            FilterChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text(stringResource(R.string.ui_wuyin)) })
        }
        when (tab) {
            0 -> {
                val w = life.wuwei
                var foods by remember { mutableStateOf(store.plate(today)) }
                val sum = PlateScorer.summary(foods, w)
                SectionCard {
                    Text(sum.line, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
                        w.flavors.forEach { f -> Text(f.name + " " + (sum.counts[f.id] ?: 0), style = MaterialTheme.typography.bodyLarge) }
                    }
                }
                w.flavors.forEach { f ->
                    Text(f.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        w.foods.filter { it.flavor == f.id }.forEach { food ->
                            FilterChip(selected = food.name in foods, onClick = {
                                foods = if (food.name in foods) foods - food.name else foods + food.name
                                store.setPlate(today, foods)
                            }, label = { Text(food.name) })
                        }
                    }
                }
                ClassicFold(w.classic, w.plain, w.classicSource)
            }
            1 -> {
                val e = life.emotions
                var entries by remember { mutableStateOf(store.diary()) }
                var pick by remember { mutableStateOf<String?>(null) }
                var level by remember { mutableStateOf(1) }
                var note by remember { mutableStateOf("") }
                val wk = DiaryStats.weekly(entries, today, e)
                SectionCard {
                    Text(wk.line, style = MaterialTheme.typography.titleMedium, fontWeight = if (wk.care) FontWeight.Bold else FontWeight.Normal)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
                        e.emotions.forEach { m -> Text(m.word + " " + (wk.counts[m.id] ?: 0), style = MaterialTheme.typography.bodyMedium) }
                    }
                }
                Text(stringResource(R.string.ui_diary_today), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    e.emotions.forEach { m -> FilterChip(selected = pick == m.id, onClick = { pick = m.id }, label = { Text(m.word) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    e.levels.forEachIndexed { i, l -> FilterChip(selected = level == i + 1, onClick = { level = i + 1 }, label = { Text(l) }) }
                }
                OutlinedTextField(value = note, onValueChange = { note = it.take(140) }, label = { Text(stringResource(R.string.ui_note)) }, modifier = Modifier.fillMaxWidth())
                Button(enabled = pick != null, onClick = {
                    store.addDiary(DiaryEntry(today, pick!!, level, note.trim()))
                    entries = store.diary(); pick = null; level = 1; note = ""
                }, modifier = Modifier.padding(top = 8.dp)) { Text(stringResource(R.string.ui_save)) }
                entries.sortedByDescending { it.date }.take(14).forEach { d ->
                    val m = e.emotions.firstOrNull { it.id == d.emotion }
                    Text("${d.date}  ${m?.word ?: d.emotion} \u00B7 ${e.levels.getOrElse(d.level - 1) { "" }}  ${d.note}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
                Text(e.privacy, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                ClassicFold(e.classic, e.plain, e.classicSource)
                ClassicFold(e.classic2, "", e.classic2Source)
            }
            else -> {
                val y = life.wuyin
                Text(y.about, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                y.tones.forEach { tn ->
                    SectionCard {
                        Text(tn.name + " \u00B7 " + tn.season + " \u00B7 " + tn.mood, style = MaterialTheme.typography.titleMedium)
                        Text(tn.classic, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        // tracks 为空时不显示播放区（不出现“准备中”）；由 :app 的播放器在有曲目时接管
                        y.tracks.filter { it.tone == tn.id }.forEach { tr -> Text(tr.title + " \u00B7 " + tr.artist, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
                Text(y.classicSource, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Footer()
    }
}
