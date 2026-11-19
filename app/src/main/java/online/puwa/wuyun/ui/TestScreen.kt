package online.puwa.wuyun.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch
import online.puwa.wuyun.R
import online.puwa.wuyun.core.life.Bi
import online.puwa.wuyun.core.life.LifeData
import online.puwa.wuyun.core.life.LifecycleCalc
import online.puwa.wuyun.core.life.WutaiQuiz
import online.puwa.wuyun.life.LocalStore
import online.puwa.wuyun.share.Share

/** P3 测一测：生命节律（上古天真）+ 五态人（灵枢·通天）。中英双语，语言跟设置走。 */
@Composable
fun TestScreen(now: Instant, life: LifeData, store: LocalStore, modifier: Modifier = Modifier) {
    var settings by remember { mutableStateOf(store.settings()) }
    val en = settings.lang == "en"
    fun t(b: Bi) = if (en) b.en else b.zh
    var tab by remember { mutableStateOf(0) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text(stringResource(R.string.ui_life)) })
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text(stringResource(R.string.ui_wutai)) })
            TextButton(onClick = {
                settings = settings.copy(lang = if (en) "zh" else "en"); store.saveSettings(settings)
            }) { Text(stringResource(if (en) R.string.ui_lang_zh else R.string.ui_lang_en)) }
        }
        if (tab == 0) {
            val f = life.lifecycle
            var yearText by remember { mutableStateOf(settings.birthYear?.toString() ?: "") }
            Text(t(f.about), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                f.tracks.forEach { tr ->
                    FilterChip(selected = settings.track == tr.id, onClick = {
                        settings = settings.copy(track = tr.id); store.saveSettings(settings)
                    }, label = { Text(t(tr.label)) })
                }
            }
            OutlinedTextField(
                value = yearText,
                onValueChange = { v ->
                    yearText = v.filter { it.isDigit() }.take(4)
                    val y = yearText.toIntOrNull()
                    if (y != null && y in 1900..2100) { settings = settings.copy(birthYear = y); store.saveSettings(settings) }
                },
                label = { Text(stringResource(R.string.ui_birth_year)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            val by = settings.birthYear
            val tr = settings.track
            if (by != null && tr != null) {
                val st = LifecycleCalc.of(by, tr, now.atZone(ZoneId.of("Asia/Shanghai")).year, f)
                val scope = rememberCoroutineScope()
                val ctx = LocalContext.current
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.ui_age, st.age), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            val cur = st.stage
                            if (cur != null) scope.launch {
                                Share.shareCustomImage(
                                    ctx,
                                    title = if (en) cur.en else cur.zh,
                                    share = if (en) f.closing.en else f.closing.zh,
                                    headline = t(f.about),
                                    text = t(f.about),
                                )
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.ui_share))
                        }
                    }
                    val cur = st.stage
                    if (cur != null) {
                        Text(if (en) cur.en else cur.zh, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(cur.classic, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    }
                    val nx = st.next
                    if (nx != null) Text(stringResource(R.string.ui_next_stage, nx.age, st.yearsToNext ?: 0), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
                st.track.stages.forEach { s ->
                    val active = st.stage?.n == s.n
                    Text(
                        "${s.age}  " + (if (en) s.en else s.zh),
                        style = if (active) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                }
                ClassicFold(st.track.classic, if (en) f.closing.en else f.closing.zh, f.source)
            }
        } else {
            val w = life.wutai
            val answers = remember { mutableStateListOf<Int>() }
            Text(t(w.about), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            if (answers.size < w.questions.size) {
                val q = w.questions[answers.size]
                LinearProgressIndicator(progress = { answers.size / w.questions.size.toFloat() }, modifier = Modifier.fillMaxWidth())
                Text(t(q.q), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 14.dp))
                q.options.forEachIndexed { i, o ->
                    OutlinedButton(onClick = { answers.add(i) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(t(o.t)) }
                }
            } else {
                val r = WutaiQuiz.score(answers.toList(), w)
                val scope = rememberCoroutineScope()
                val ctx = LocalContext.current
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t(r.type.name), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            scope.launch {
                                Share.shareCustomImage(
                                    ctx,
                                    title = t(r.type.title),
                                    share = t(r.type.plain),
                                    headline = t(w.about),
                                    text = t(r.type.plain),
                                )
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.ui_share))
                        }
                    }
                    Text(t(r.type.title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(t(r.type.plain), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                }
                ClassicFold(r.type.classic, "", w.source, r.type.classicNote)
                Button(onClick = { answers.clear() }, modifier = Modifier.padding(top = 8.dp)) { Text(stringResource(R.string.ui_retry)) }
            }
        }
        Footer()
    }
}
