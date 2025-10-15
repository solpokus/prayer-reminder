
package com.example.prayerreminder.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.prayerreminder.alarm.AlarmScheduler
import com.example.prayerreminder.di.ServiceLocator
import com.example.prayerreminder.model.PrayerTime
import com.example.prayerreminder.repo.PrayerRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class PrayerState(
    val loading: Boolean = false,
    val error: String? = null,
    val location: Pair<Double, Double>? = null,
    val timings: List<PrayerTime> = emptyList(),
    val next: PrayerTime? = null
)

class PrayerVM(private val app: Application) : ViewModel() {

    private val repo = PrayerRepo(ServiceLocator.api())
    private val _state = MutableStateFlow(PrayerState())
    val state = _state.asStateFlow()

    fun loadTimings(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, location = lat to lng)
            runCatching { repo.timings(lat, lng) }
                .onSuccess { resp ->
                    val tz = resp.data.meta.timezone ?: ZoneId.systemDefault().id
                    val zone = runCatching { ZoneId.of(tz) }.getOrDefault(ZoneId.systemDefault())
                    val today = LocalDate.now(zone)

                    fun parse(name: String, raw: String?): PrayerTime? {
                        if (raw.isNullOrBlank()) return null
                        val timeStr = raw.split(" ")[0]
                        val parts = timeStr.split(":")
                        if (parts.size < 2) return null
                        val h = parts[0].toIntOrNull() ?: return null
                        val m = parts[1].toIntOrNull() ?: return null
                        val zdt = ZonedDateTime.of(today, LocalTime.of(h, m), zone)
                        return PrayerTime(name, zdt)
                    }

                    val list = listOfNotNull(
                        parse("Fajr", resp.data.timings.Fajr),
                        parse("Sunrise", resp.data.timings.Sunrise),
                        parse("Dhuhr", resp.data.timings.Dhuhr),
                        parse("Asr", resp.data.timings.Asr),
                        parse("Maghrib", resp.data.timings.Maghrib),
                        parse("Isha", resp.data.timings.Isha),
                    ).sortedBy { it.zdt.toInstant() }

                    val now = ZonedDateTime.now(zone).toInstant().toEpochMilli()
                    val next = list.firstOrNull { it.zdt.toInstant().toEpochMilli() > now && it.name != "Sunrise" }

                    _state.value = _state.value.copy(loading = false, timings = list, next = next)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
        }
    }

    fun scheduleTodayReminders(ctx: Context) {
        val s = _state.value
        val zone = s.timings.firstOrNull()?.zdt?.zone ?: ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone).toInstant().toEpochMilli()
        val upcoming = s.timings.filter { it.name != "Sunrise" && it.zdt.toInstant().toEpochMilli() > now }
        if (upcoming.isNotEmpty()) {
            val am = ServiceLocator.alarmManager(ctx)
            AlarmScheduler(am).scheduleMany(ctx, upcoming)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PrayerVM(app) as T
    }
}
