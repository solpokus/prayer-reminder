
package com.example.prayerreminder.net

import com.example.prayerreminder.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {
    // https://api.aladhan.com/v1/timings?latitude=...&longitude=...&method=4
    @GET("v1/timings")
    suspend fun timings(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("method") method: Int = 4, // Umm al-Qura University, Makkah
        @Query("school") school: Int = 0
    ): ApiResponse
}
