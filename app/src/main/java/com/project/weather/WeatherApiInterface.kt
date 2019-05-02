package com.project.weather

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiInterface {
    @GET("weather?lat=33.7748&lon=84.2963")
    fun getWeatherDataAsync(
        @Query("appid") appId: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("unit") unit: String
    ): Deferred<Response<WeatherModelList>>
}