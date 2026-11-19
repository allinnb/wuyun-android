package online.puwa.wuyun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import online.puwa.wuyun.R
import online.puwa.wuyun.core.life.LifeData
import online.puwa.wuyun.core.life.SeasonCalc
import online.puwa.wuyun.core.life.ShichenCalc
import online.puwa.wuyun.life.LocalStore
import online.puwa.wuyun.remind.ReminderScheduler

/** P2 时令页：四季作息（四气调神）+ 十二时辰钟 + 交节提醒开关。 */
@Composable
fun SeasonScreen(now: Instant, life: LifeData, store: LocalStore, modifier: Modifier = Modifier) {
    val s = SeasonCalc.of(now, life.seasons)
    val sc = ShichenCalc.of(now, life.shichen)
    val context = LocalContext.current
    var settings by remember { mutableStateOf(store.settings()) }
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted: Boolean ->
        settings = settings.copy(remind = granted)
        store.saveSettings(settings)
        if (granted) ReminderScheduler.scheduleNext(context) else ReminderScheduler.cancel(context)
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        // 交节提醒开关
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            Switch(
                checked = settings.remind,
                onCheckedChange = { on ->
                    if (on && android.os.Build.VERSION.SDK_INT >= 33 &&
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.POST_NOTIFICATIONS,
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        settings = settings.copy(remind = on)
                        store.saveSettings(settings)
                        if (on) ReminderScheduler.scheduleNext(context) else ReminderScheduler.cancel(context)
                    }
                },
            )
            Text(
                stringResource(R.string.ui_remind),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        // 四季
        Text(s.season.name + " \u00B7 " + s.season.word, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(s.season.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Text(
            stringResource(R.string.ui_season_range, s.fromTime.substring(0, 10), s.toTime.substring(0, 10), s.dayNo),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionCard {
            Text(stringResource(R.string.ui_sleep), style = MaterialTheme.typography.labelMedium)
            Text(s.season.sleep, style = MaterialTheme.typography.titleLarge)
            Text(s.season.body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        }
        SectionCard {
            Text(stringResource(R.string.ui_todo), style = MaterialTheme.typography.labelMedium)
            s.season.todo.forEach { Text("\u00B7 $it", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp)) }
        }
        ClassicFold(s.season.classic, s.season.plain, life.seasons.source, s.season.classicNote)

        // 十二时辰
        Text(stringResource(R.string.ui_shichen), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
        SectionCard {
            Text(stringResource(R.string.ui_shichen_now, sc.item.branch, sc.item.range), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(sc.item.title, style = MaterialTheme.typography.titleLarge)
            Text(sc.item.tip, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
            Text(
                stringResource(R.string.ui_shichen_left, sc.item.meridian, sc.minutesLeft),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp),
            )
        }
        life.shichen.items.forEachIndexed { i, it ->
            Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(10.dp).background(
                        if (i == sc.index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape,
                    ),
                )
                Text(it.branch, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 10.dp))
                Text(it.range, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                Text(it.tip, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp))
            }
        }
        ClassicFold(life.shichen.classic, life.shichen.plain, life.shichen.classicSource)
        Text(life.shichen.about, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Footer()
    }
}
