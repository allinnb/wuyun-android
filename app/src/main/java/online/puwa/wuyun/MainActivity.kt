package online.puwa.wuyun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * P0 阶段：空的 Compose 工程壳。只验证 assets 数据加载可走通，不做界面。
 */
class MainActivity : ComponentActivity() {

    private val repository: WuyunRepository by lazy { WuyunRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 数据层冒烟：assets -> :core 解析
        repository.data
        setContent {
            WuyunTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(Modifier)
                }
            }
        }
    }
}

@Composable
fun WuyunTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
