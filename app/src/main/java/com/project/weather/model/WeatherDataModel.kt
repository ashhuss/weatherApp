package com.project.weather.model

import com.google.gson.annotations.SerializedName

data class WeatherDataModel(
    @SerializedName("main")
    var main: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("country")
    var country: String,
    @SerializedName("temp")
    var temp: Double,
    @SerializedName("pressure")
    var pressure: Double,
    @SerializedName("humidity")
    var humidity: Int,
    @SerializedName("temp_max")
    var tempMax: Double,
    @SerializedName("temp_min")
    var tempMin: Double,
    @SerializedName("sea_level")
    var seaLevel: Double,
    @SerializedName("speed")
    var speed: Double,
    @SerializedName("deg")
    var deg: Double,
    @SerializedName("all")
    var all: Int,
    @SerializedName("grnd_level")
    var groundLevel: Double
)
