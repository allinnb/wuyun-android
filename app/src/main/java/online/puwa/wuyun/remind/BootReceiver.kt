package online.puwa.wuyun.remind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 开机后重排交节提醒（RECEIVE_BOOT_COMPLETED）。 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.scheduleNext(context)
        }
    }
}
