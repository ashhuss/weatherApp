package com.project.weather.common

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.project.weather.model.WeatherApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitSetup {

    object RetrofitFactory {
        const val BASE_URL = "http://api.openweathermap.org/data/2.5/"

        fun makeRetrofitService(): WeatherApiInterface {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build().create(WeatherApiInterface::class.java)
        }
    }
}