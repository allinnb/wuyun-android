package online.puwa.wuyun.remind

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import online.puwa.wuyun.MainActivity
import online.puwa.wuyun.R
import online.puwa.wuyun.life.LocalStore

/**
 * 交节提醒广播：开启时发通知（标题 = stepName，正文 = strings.xml 的 ui_notify_body），
 * 然后顺手排下一条。无通知权限时静默跳过并重排。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val store = LocalStore(context)
        if (!store.settings().remind) return

        val stepName = intent.getStringExtra("stepName") ?: return
        val boundary = intent.getStringExtra("boundary") ?: return
        val boundaryTime = intent.getStringExtra("boundaryTime") ?: ""

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ReminderScheduler.scheduleNext(context)
            return
        }

        val nm = NotificationManagerCompat.from(context)
        val channelId = "remind"
        val ch = android.app.NotificationChannel(
            channelId,
            context.getString(R.string.ui_notify_channel),
            android.app.NotificationManager.IMPORTANCE_DEFAULT,
        )
        nm.createNotificationChannel(ch)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val date = if (boundaryTime.length >= 10) boundaryTime.substring(0, 10) else boundaryTime
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(stepName)
            .setContentText(context.getString(R.string.ui_notify_body, date, boundary))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(NOTIFY_ID, notif)

        ReminderScheduler.scheduleNext(context)
    }

    companion object {
        const val NOTIFY_ID = 1001
    }
}
