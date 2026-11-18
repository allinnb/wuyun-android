package online.puwa.wuyun.core

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * JVM（AWT）版分享图渲染器，仅用于 :core 单元测试生成 PNG。
 * app 端使用 android.graphics.Canvas 的实现。
 */
object AwtShareRenderer : ShareImageRenderer {

    private fun cjkFont(size: Int, bold: Boolean): Font {
        val candidates = listOf(
            "C:/Windows/Fonts/msyh.ttc",
            "C:/Windows/Fonts/simsun.ttc",
            "/System/Library/Fonts/PingFang.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
        )
        for (p in candidates) {
            val f = File(p)
            if (f.exists()) {
                return runCatching {
                    Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(Font.PLAIN, size.toFloat())
                }.getOrDefault(Font("SansSerif", if (bold) Font.BOLD else Font.PLAIN, size))
            }
        }
        return Font("SansSerif", if (bold) Font.BOLD else Font.PLAIN, size)
    }

    override fun renderPng(spec: ShareImageSpec): ByteArray {
        val img = BufferedImage(spec.width, spec.height, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.color = Color.WHITE
            g.fillRect(0, 0, spec.width, spec.height)

            val m = ShareImages.MARGIN
            // 顶部强调色条
            g.color = Color.decode(spec.accentHex)
            g.fillRect(0, 0, spec.width, 24)

            var y = 150f
            g.font = cjkFont(72, bold = true)
            g.color = Color(0x1A, 0x1A, 0x1A)
            for (line in spec.titleLines) {
                g.drawString(line, m.toFloat(), y)
                y += 92
            }
            y += 48
            g.font = cjkFont(44, bold = false)
            g.color = Color(0x33, 0x33, 0x33)
            for (line in spec.shareLines) {
                g.drawString(line, m.toFloat(), y)
                y += 62
            }
            y += 48
            g.font = cjkFont(36, bold = false)
            g.color = Color(0x66, 0x66, 0x66)
            for (line in spec.headlineLines) {
                g.drawString(line, m.toFloat(), y)
                y += 52
            }

            // 卡底小字
            g.font = cjkFont(30, bold = false)
            g.color = Color(0x99, 0x99, 0x99)
            g.drawString(spec.footer, m.toFloat(), (spec.height - 140).toFloat())

            // 二维码（右下角）
            val qrSize = 300
            val matrix = QRCodeWriter().encode(spec.url, BarcodeFormat.QR_CODE, qrSize, qrSize)
            val scale = qrSize.toFloat() / matrix.width
            val qrX = spec.width - m - qrSize
            val qrY = spec.height - 140 - qrSize
            g.color = Color.WHITE
            g.fillRect(qrX, qrY, qrSize, qrSize)
            g.color = Color(0x1A, 0x1A, 0x1A)
            for (mx in 0 until matrix.width) {
                for (my in 0 until matrix.height) {
                    if (matrix.get(mx, my)) {
                        g.fillRect(
                            (qrX + mx * scale).toInt(), (qrY + my * scale).toInt(),
                            scale.toInt() + 1, scale.toInt() + 1,
                        )
                    }
                }
            }
        } finally {
            g.dispose()
        }
        return ByteArrayOutputStream().use {
            ImageIO.write(img, "png", it)
            it.toByteArray()
        }
    }
}
