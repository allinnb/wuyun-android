package online.puwa.wuyun.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.puwa.wuyun.R

/** 通用小块：标题卡、原文折叠。各页共用，保持与今日卡一致的留白。 */
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) { Column(Modifier.padding(18.dp)) { content() } }
}

@Composable
fun ClassicFold(classic: String, plain: String, source: String, note: String = "") {
    var open by remember { mutableStateOf(false) }
    SectionCard {
        Text(
            stringResource(R.string.ui_classic) + if (open) " \u25B4" else " \u25BE",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth().clickable { open = !open },
        )
        if (open) {
            Text(classic, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            if (plain.isNotEmpty()) {
                Text(stringResource(R.string.ui_plain), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp))
                Text(plain, style = MaterialTheme.typography.bodyMedium)
            }
            if (source.isNotEmpty()) Text(source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            if (note.isNotEmpty()) {
                Text(stringResource(R.string.ui_variants), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp))
                Text(note, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun Footer() {
    Text(
        stringResource(R.string.ui_footer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    )
}
