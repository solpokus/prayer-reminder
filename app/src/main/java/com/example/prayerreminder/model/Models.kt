
package com.example.prayerreminder.model

import java.time.ZonedDateTime

data class PrayerTime(
    val name: String,
    val zdt: ZonedDateTime
) {
    val timeDisplay: String get() = "%02d:%02d".format(zdt.hour, zdt.minute)
}

data class ApiTimings(
    val Fajr: String?,
    val Sunrise: String?,
    val Dhuhr: String?,
    val Asr: String?,
    val Maghrib: String?,
    val Isha: String?
)

data class ApiMeta(val timezone: String?)
data class ApiData(val timings: ApiTimings, val meta: ApiMeta)
data class ApiResponse(val code: Int, val status: String, val data: ApiData)
