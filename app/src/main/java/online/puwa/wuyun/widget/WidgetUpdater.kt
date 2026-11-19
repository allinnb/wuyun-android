package online.puwa.wuyun.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import online.puwa.wuyun.MainActivity
import online.puwa.wuyun.R
import online.puwa.wuyun.WuyunRepository
import online.puwa.wuyun.core.TodayPresenter
import online.puwa.wuyun.core.life.ShichenCalc

/** 小组件内容：今日卡 title + headline + 当前时辰与 tip。点按打开 App。 */
object WidgetUpdater {

    private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")

    fun updateAll(context: Context) {
        val app = context.applicationContext
        val mgr = AppWidgetManager.getInstance(app)
        val ids = mgr.getAppWidgetIds(ComponentName(app, WuyunWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val repo = WuyunRepository(app)
        val now = Instant.now()
        val state = TodayPresenter.build(now, repo.data, null)
        val sc = ShichenCalc.of(now, repo.life.shichen)

        val views = RemoteViews(app.packageName, R.layout.widget_wuyun)
        views.setTextViewText(R.id.widget_title, state.card.title)
        views.setTextViewText(R.id.widget_headline, state.headline)
        views.setTextViewText(
            R.id.widget_shichen,
            app.getString(R.string.ui_widget_shichen, sc.item.branch, sc.item.tip),
        )
        val pi = PendingIntent.getActivity(
            app, 0, Intent(app, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_root, pi)
        mgr.updateAppWidget(ids, views)
    }

    /** 下一次更新时刻：30 分钟内与下一个时辰交界中较早者。 */
    fun nextUpdateAt(now: Instant): Instant {
        val t = now.atZone(SHANGHAI).toLocalTime()
        // 时辰交界在奇数小时（23,1,3,...,21）
        var boundary = t.withMinute(0).withSecond(0).withNano(0).plusHours(1)
        while (boundary.hour % 2 == 0) boundary = boundary.plusHours(1)
        val bInstant = boundary.atDate(LocalDateTime.ofInstant(now, SHANGHAI).toLocalDate()).atZone(SHANGHAI).toInstant()
        val half = now.plusSeconds(30 * 60)
        return if (bInstant.isBefore(half)) bInstant else half
    }
}
