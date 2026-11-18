package online.puwa.wuyun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import online.puwa.wuyun.core.TodayPresenter
import online.puwa.wuyun.core.WuyunCalc
import online.puwa.wuyun.theme.QiPalettes
import online.puwa.wuyun.theme.WuyunTheme
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

@androidx.compose.runtime.Composable
private fun WuyunApp(initial: Instant) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { WuyunRepository(context) }
    val data = repository.data
    val tokens = remember { QiPalettes.loadTokens(context) }
    var selectedStep by remember { mutableStateOf<Int?>(null) }
    val state = remember(initial, selectedStep) {
        TodayPresenter.build(initial, data, selectedStep)
    }
    val dark = isSystemInDarkTheme()
    val token = tokens[state.card.keqi] ?: "lan"
    WuyunTheme(token = token, darkTheme = dark) {
        TodayScreen(
            state = state,
            accentHex = QiPalettes.hex(token, dark),
            onSelectStep = { selectedStep = it },
        )
    }
}
