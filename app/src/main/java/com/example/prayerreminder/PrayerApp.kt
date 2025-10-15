
package com.example.prayerreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PrayerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "prayer_channel",
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.channel_desc) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
