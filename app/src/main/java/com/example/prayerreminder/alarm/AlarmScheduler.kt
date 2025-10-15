
package com.example.prayerreminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.prayerreminder.model.PrayerTime

class AlarmScheduler(private val alarmManager: AlarmManager) {
    fun scheduleMany(ctx: Context, upcoming: List<PrayerTime>) {
        upcoming.forEachIndexed { idx, p ->
            val intent = Intent(ctx, PrayerAlarmReceiver::class.java).apply {
                putExtra("title", "${p.name} time")
                putExtra("content", "It's time for ${p.name}")
            }
            val pi = PendingIntent.getBroadcast(
                ctx, 2000 + idx, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val at = p.zdt.toInstant().toEpochMilli()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, at, pi)
            }
        }
    }
}
