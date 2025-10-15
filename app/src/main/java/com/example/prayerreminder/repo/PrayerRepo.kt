
package com.example.prayerreminder.repo

import com.example.prayerreminder.model.ApiResponse
import com.example.prayerreminder.net.AladhanApi

class PrayerRepo(private val api: AladhanApi) {
    suspend fun timings(lat: Double, lng: Double): ApiResponse = api.timings(lat, lng, method = 4)
}
