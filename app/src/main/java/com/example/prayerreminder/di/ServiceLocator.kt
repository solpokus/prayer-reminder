
package com.example.prayerreminder.di

import android.app.AlarmManager
import android.content.Context
import com.example.prayerreminder.net.AladhanApi
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceLocator {
    fun api(): AladhanApi {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()
        val moshi = Moshi.Builder().build()
        return Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(AladhanApi::class.java)
    }

    fun alarmManager(ctx: Context): AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
