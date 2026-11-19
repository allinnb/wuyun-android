package online.puwa.wuyun.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import java.time.Instant

/**
 * 桌面小组件。系统每 30 分钟（updatePeriodMillis）回调 onUpdate；
 * 另用 AlarmManager 在“30 分钟 / 下一个时辰交界”的较早时刻补一次更新。
 */
class WuyunWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateAll(context)
        scheduleTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TICK) {
            WidgetUpdater.updateAll(context)
            scheduleTick(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onDisabled(context: Context) {
        cancelTick(context)
    }

    private fun scheduleTick(context: Context) {
        val app = context.applicationContext
        val at = WidgetUpdater.nextUpdateAt(Instant.now())
        val pi = tickPendingIntent(app)
        app.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pi)
    }

    private fun cancelTick(context: Context) {
        context.applicationContext.getSystemService(AlarmManager::class.java).cancel(tickPendingIntent(context.applicationContext))
    }

    private fun tickPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WuyunWidgetProvider::class.java).apply { action = ACTION_TICK }
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val ACTION_TICK = "online.puwa.wuyun.action.WIDGET_TICK"
    }
}
