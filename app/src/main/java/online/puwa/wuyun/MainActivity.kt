package online.puwa.wuyun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.time.Instant
import online.puwa.wuyun.core.TodayPresenter
import online.puwa.wuyun.core.WuyunCalc
import online.puwa.wuyun.life.LocalStore
import online.puwa.wuyun.theme.QiPalettes
import online.puwa.wuyun.theme.WuyunTheme
import online.puwa.wuyun.ui.RecordScreen
import online.puwa.wuyun.ui.SeasonScreen
import online.puwa.wuyun.ui.TestScreen
import online.puwa.wuyun.ui.TodayScreen

/**
 * P1：今日卡 + 六步时间轴 + 原文展开 + 说明弹窗 + 分享。
 * debug 版接受 intent extra "date"（格式同 WuyunCalc.parseDateParam）用于注入日期：
 *   adb shell am start -n online.puwa.wuyun/.MainActivity --es date 2028-01-21T12:00
 * 非法或为空时用当前时间；release 版忽略该参数。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initial: Instant = if (BuildConfig.DEBUG) {
            WuyunCalc.parseDateParam(intent?.getStringExtra(EXTRA_DATE)) ?: Instant.now()
        } else {
            Instant.now()
        }
        setContent { WuyunApp(initial) }
    }

    companion object {
        const val EXTRA_DATE = "date"
    }
}

@Composable
private fun WuyunApp(initial: Instant) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { WuyunRepository(context) }
    val data = repository.data
    val life = remember { repository.life }
    val store = remember { LocalStore(context) }
    val tokens = remember { QiPalettes.loadTokens(context) }
    var selectedStep by remember { mutableStateOf<Int?>(null) }
    var tab by remember { mutableStateOf(0) }
    val state = remember(initial, selectedStep) {
        TodayPresenter.build(initial, data, selectedStep)
    }
    val dark = isSystemInDarkTheme()
    val token = tokens[state.card.keqi] ?: "lan"
    WuyunTheme(token = token, darkTheme = dark) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TABS.forEachIndexed { i, (icon, label) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(stringResource(label)) },
                        )
                    }
                }
            },
        ) { pad ->
            val m = Modifier.padding(pad)
            when (tab) {
                0 -> Box(m) {
                    TodayScreen(state = state, accentHex = QiPalettes.hex(token, dark), onSelectStep = { selectedStep = it })
                }
                1 -> SeasonScreen(initial, life, store, m)
                2 -> TestScreen(initial, life, store, m)
                else -> RecordScreen(initial, life, store, m)
            }
        }
    }
}

private val TABS = listOf(
    Icons.Filled.Home to R.string.tab_today,
    Icons.Filled.DateRange to R.string.tab_season,
    Icons.Filled.Face to R.string.tab_test,
    Icons.Filled.Edit to R.string.tab_record,
)
