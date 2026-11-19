package online.puwa.wuyun.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import online.puwa.wuyun.R
import online.puwa.wuyun.core.ShareImages
import online.puwa.wuyun.core.TodayState

/** 分享：只发文字（shareText），或发分享图（1080×1440 PNG + FileProvider，不申请存储权限）。 */
object Share {

    private const val AUTHORITY = "online.puwa.wuyun.fileprovider"

    fun shareTextOnly(context: Context, state: TodayState) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, state.shareText)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.ui_share)))
    }

    suspend fun shareImage(context: Context, state: TodayState, accentHex: String) {
        val url = state.shareText.substringAfter('\n')
        val png = withContext(Dispatchers.Default) {
            CanvasShareRenderer.renderPng(ShareImages.fromState(state, context.getString(R.string.ui_footer), url, accentHex))
        }
        writeAndShare(context, png, state.shareText)
    }

    /**
     * P3 分享图：生命节律 / 五态人结果。1080×1440 PNG，二维码指向官网首页。
     * title/share/headline 由调用方按当前语言从 JSON 取。
     */
    suspend fun shareCustomImage(context: Context, title: String, share: String, headline: String, text: String) {
        val png = withContext(Dispatchers.Default) {
            CanvasShareRenderer.renderPng(
                ShareImages.build(
                    title = title,
                    share = share,
                    headline = headline,
                    footer = context.getString(R.string.ui_footer),
                    url = BASE_URL,
                    accentHex = "#1565C0",
                ),
            )
        }
        writeAndShare(context, png, text)
    }

    private suspend fun writeAndShare(context: Context, png: ByteArray, text: String) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "wuyun-share.png")
        file.writeBytes(png)
        val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        withContext(Dispatchers.Main) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.ui_share)))
        }
    }

    private const val BASE_URL = "https://allinnb.github.io/wuyun/"
}
