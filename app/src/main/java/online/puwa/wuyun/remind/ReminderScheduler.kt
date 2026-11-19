package online.puwa.wuyun.remind

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import online.puwa.wuyun.core.life.ReminderPlanner
import online.puwa.wuyun.life.LocalStore

/**
 * 交节提醒调度：用 ReminderPlanner.next(now) 取下一条，AlarmManager.setAndAllowWhileIdle 在
 * 交节前 3 天 09:00（北京时间）触发 ReminderReceiver。不需要 SCHEDULE_EXACT_ALARM。
 */
object ReminderScheduler {

    const val ACTION_REMIND = "online.puwa.wuyun.action.REMIND"

    private val SHANGHAI: ZoneId = ZoneId.of("Asia/Shanghai")
    private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun remindAtToEpochMs(remindAt: String): Long =
        LocalDateTime.parse(remindAt, FMT).atZone(SHANGHAI).toInstant().toEpochMilli()

    private fun pendingIntent(context: Context, next: online.puwa.wuyun.core.life.Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            putExtra("step", next.step)
            putExtra("stepName", next.stepName)
            putExtra("boundary", next.boundary)
            putExtra("boundaryTime", next.boundaryTime)
            putExtra("remindAt", next.remindAt)
        }
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** 按设置重排下一条提醒；设置关闭时取消。 */
    fun scheduleNext(context: Context) {
        val app = context.applicationContext
        val store = LocalStore(app)
        val am = app.getSystemService(AlarmManager::class.java)
        if (!store.settings().remind) {
            cancel(app)
            return
        }
        val next = ReminderPlanner.next(Instant.now(), 1).firstOrNull() ?: return
        am.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            remindAtToEpochMs(next.remindAt),
            pendingIntent(app, next),
        )
    }

    fun cancel(context: Context) {
        val app = context.applicationContext
        val am = app.getSystemService(AlarmManager::class.java)
        val intent = Intent(app, ReminderReceiver::class.java).apply { action = ACTION_REMIND }
        am.cancel(PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }
}
