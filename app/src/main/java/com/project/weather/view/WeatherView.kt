package com.project.weather.view

import com.project.weather.model.WeatherDataModel

 interface WeatherView {

     fun checkpoint()
     fun settingValues()
     fun noInternetDialog()
     fun buildAlertMessageNoGps()
     fun getLastLocation()
     fun save(latValue:String, lonValue:String)
     fun saveWeatherData(country:String,
                         humidity:String,
                         maxTamp:String,
                         minTemp:String,
                         pressure:String,
                         speed:String,
                         deg:String,
                         cloudAll:String,
                         temp:String,
                         current:String,
                         main:String)

      fun showWeatherData(
          weatherModels: List<WeatherDataModel>?,
          weatherDataModelMain: WeatherDataModel,
          weatherDataModelWind: WeatherDataModel?,
          weatherDataModelSys: WeatherDataModel?,
          weatherDataModelCloud: WeatherDataModel?
      )
}