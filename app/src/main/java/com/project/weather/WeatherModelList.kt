package com.project.weather

import com.google.gson.annotations.SerializedName

data class WeatherModelList(

    @SerializedName("weather")
    val weatherDataList: List<WeatherDataModel> = emptyList(),

    @SerializedName("main")
    val mainData: WeatherDataModel
) {
    fun getWeatherList(): List<WeatherDataModel> {
        return weatherDataList
    }

}

