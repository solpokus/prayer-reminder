
# PrayerReminder (Kotlin + Compose)

A minimal **Prayer Reminder** app that:
- Detects your **current location**
- Fetches **today's prayer times** from AlAdhan with **Umm al-Qura (Makkah) method**
- Shows the next prayer and lets you **schedule notifications** for the remaining prayers today

## Run it
1. Open **Android Studio → Open** and choose this folder.
2. Let it **sync Gradle** (Android Studio may create/upgrade the Gradle wrapper).
3. Click **Run ▶** and pick an emulator or device (API 30+ recommended).

## Notes
- Endpoint used: `https://api.aladhan.com/v1/timings?latitude=...&longitude=...&method=4` (Umm al-Qura).
- On **Android 13+**, you'll be asked for **POST_NOTIFICATIONS**.
- On **Android 12/13/14**, exact alarms require the **SCHEDULE_EXACT_ALARM** special access. Use the **"Exact alarms settings"** button to allow it for precise reminders.

## Stack
- Kotlin 1.9.24, AGP 8.3.2
- Jetpack Compose Material 3
- Retrofit + Moshi, OkHttp
- Google Play Services Location
- Min SDK 24, Target 34
