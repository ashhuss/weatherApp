package com.project.weather.presenter

interface WeatherPresenter {
    fun loadWeatherData(appId: String, lat: String, lon: String, unit: String)
}