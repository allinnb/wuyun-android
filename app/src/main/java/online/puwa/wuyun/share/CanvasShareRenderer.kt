package online.puwa.wuyun.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import online.puwa.wuyun.core.ShareImageRenderer
import online.puwa.wuyun.core.ShareImageSpec
import online.puwa.wuyun.core.ShareImages

/** app 端分享图渲染器：1080×1440 竖版 PNG，Canvas 绘制，zxing 生成二维码。布局（换行）来自 :core 的 ShareImages。 */
object CanvasShareRenderer : ShareImageRenderer {

    override fun renderPng(spec: ShareImageSpec): ByteArray {
        val bmp = createBitmap(spec.width, spec.height)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)
        val m = ShareImages.MARGIN

        val barPaint = Paint().apply { color = spec.accentHex.toColorInt() }
        c.drawRect(0f, 0f, spec.width.toFloat(), 24f, barPaint)

        var y = 150f
        val titlePaint = Paint().apply {
            color = "#1A1A1A".toColorInt()
            textSize = 72f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        for (line in spec.titleLines) {
            c.drawText(line, m.toFloat(), y, titlePaint)
            y += 92
        }
        y += 48
        val sharePaint = Paint().apply {
            color = "#333333".toColorInt()
            textSize = 44f
            isAntiAlias = true
        }
        for (line in spec.shareLines) {
            c.drawText(line, m.toFloat(), y, sharePaint)
            y += 62
        }
        y += 48
        val headPaint = Paint().apply {
            color = "#666666".toColorInt()
            textSize = 36f
            isAntiAlias = true
        }
        for (line in spec.headlineLines) {
            c.drawText(line, m.toFloat(), y, headPaint)
            y += 52
        }

        val footPaint = Paint().apply {
            color = "#999999".toColorInt()
            textSize = 30f
            isAntiAlias = true
        }
        c.drawText(spec.footer, m.toFloat(), (spec.height - 140).toFloat(), footPaint)

        val qrSize = 300
        val matrix = QRCodeWriter().encode(spec.url, BarcodeFormat.QR_CODE, qrSize, qrSize)
        val scale = qrSize.toFloat() / matrix.width
        val qrX = (spec.width - m - qrSize).toFloat()
        val qrY = (spec.height - 140 - qrSize).toFloat()
        val whitePaint = Paint().apply { color = Color.WHITE }
        c.drawRect(qrX, qrY, qrX + qrSize, qrY + qrSize, whitePaint)
        val blackPaint = Paint().apply { color = "#1A1A1A".toColorInt() }
        for (mx in 0 until matrix.width) {
            for (my in 0 until matrix.height) {
                if (matrix.get(mx, my)) {
                    c.drawRect(
                        qrX + mx * scale, qrY + my * scale,
                        qrX + (mx + 1) * scale + 1, qrY + (my + 1) * scale + 1,
                        blackPaint,
                    )
                }
            }
        }

        return ByteArrayOutputStream().use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.toByteArray()
        }
    }
}
