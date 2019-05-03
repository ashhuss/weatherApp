package com.project.weather.model

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiInterface {
    @GET("weather")
    fun getWeatherDataAsync(
        @Query("appid") appId: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("unit") unit: String
    ): Deferred<Response<WeatherModelList>>
}