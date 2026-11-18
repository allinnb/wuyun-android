package online.puwa.wuyun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import online.puwa.wuyun.R
import online.puwa.wuyun.core.Card
import online.puwa.wuyun.core.TodayState
import online.puwa.wuyun.core.TimelineItem
import online.puwa.wuyun.share.Share
import online.puwa.wuyun.theme.LocalQiPalette

@Composable
fun TodayScreen(
    state: TodayState,
    accentHex: String,
    onSelectStep: (Int) -> Unit,
) {
    val context = LocalContext.current
    val palette = LocalQiPalette.current
    val scope = rememberCoroutineScope()
    var showInfo by remember { mutableStateOf(false) }
    var showShareMenu by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.ui_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // 顶部：headline 一行，右侧 ⓘ 和分享
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
                IconButton(onClick = { showShareMenu = true }) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.ui_share))
                }
            }
            Spacer(Modifier.height(12.dp))

            // 主卡片
            Surface(
                color = palette.accentSoft,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = state.card.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.accent,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.rangeLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.yearLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.card.sky,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.card.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(16.dp))

                    // 喝 / 按 / 动 三个小卡
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniCard(stringResource(R.string.ui_drink), state.card.drink, Modifier.weight(1f))
                        MiniCard(stringResource(R.string.ui_press), state.card.press, Modifier.weight(1f))
                        MiniCard(stringResource(R.string.ui_move), state.card.move, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))

                    ClassicSection(state.card)
                }
            }
            Spacer(Modifier.height(16.dp))

            // 六步时间轴（横滚），点选切换上面的卡
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.timeline.forEachIndexed { index, item ->
                    TimelineCell(
                        item = item,
                        onClick = { onSelectStep(index + 1) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            text = { Text(stringResource(R.string.ui_info_text)) },
            confirmButton = {
                IconButton(onClick = { showInfo = false }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            },
        )
    }
    if (showShareMenu) {
        AlertDialog(
            onDismissRequest = { showShareMenu = false },
            title = { Text(stringResource(R.string.ui_share)) },
            text = {
                Column {
                    TextButton(onClick = {
                        showShareMenu = false
                        Share.shareTextOnly(context, state)
                    }) {
                        Text(stringResource(R.string.ui_share_text))
                    }
                    TextButton(onClick = {
                        showShareMenu = false
                        scope.launch { Share.shareImage(context, state, accentHex) }
                    }) {
                        Text(stringResource(R.string.ui_share_image))
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun MiniCard(title: String, content: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = LocalQiPalette.current.accent,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ClassicSection(card: Card) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Text(
            text = stringResource(R.string.ui_classic),
            style = MaterialTheme.typography.titleSmall,
            color = LocalQiPalette.current.accent,
        )
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = card.classic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.ui_plain),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = card.plain,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = card.classic_source,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (card.classic_note.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.ui_variants),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = card.classic_note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun TimelineCell(item: TimelineItem, onClick: () -> Unit) {
    val palette = LocalQiPalette.current
    Surface(
        color = if (item.active) palette.accentSoft else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (item.active) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.qi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
