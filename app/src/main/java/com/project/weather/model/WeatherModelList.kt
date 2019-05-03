package com.project.weather.model

import com.google.gson.annotations.SerializedName

data class WeatherModelList(

    @SerializedName("weather")
    val weatherDataList: List<WeatherDataModel> = emptyList(),

    @SerializedName("main")
    val mainData: WeatherDataModel,

    @SerializedName("clouds")
    val cloudData: WeatherDataModel,

    @SerializedName("wind")
    val wind: WeatherDataModel,

    @SerializedName("rain")
    val rain: WeatherDataModel,

    @SerializedName("sys")
    val sys: WeatherDataModel
) { fun getWeatherList(): List<WeatherDataModel> { return weatherDataList } }

