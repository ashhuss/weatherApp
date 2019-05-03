package com.project.weather.presenter

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.project.weather.common.RetrofitSetup
import com.project.weather.model.WeatherModelList
import com.project.weather.view.WeatherView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class WeatherImp(var view: WeatherView?, var context: Context) : WeatherPresenter {


    override fun loadWeatherData(appId: String, lat: String, lon: String, unit: String) {
        val service = RetrofitSetup.RetrofitFactory.makeRetrofitService()
        CoroutineScope(Dispatchers.IO).launch {
            val request = service.getWeatherDataAsync(appId, lat, lon, unit)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    if (response.isSuccessful) {
                        context.toast("Success")
                        val weatherList: WeatherModelList? = response.body()
                        val weatherDataModel = weatherList?.getWeatherList()
                        val weatherDataModelMain = weatherList?.mainData
                        val weatherDataModelWind = weatherList?.wind
                        val weatherDataModelSys = weatherList?.sys
                        val weatherDataModelCloud = weatherList?.cloudData

                        view?.showWeatherData(
                            weatherDataModel,
                            weatherDataModelMain!!,
                            weatherDataModelWind,
                            weatherDataModelSys,
                            weatherDataModelCloud
                        )

                    } else {
                        context.toast("Error: ${response.code()}")
                    }
                } catch (e: HttpException) {
                    context.toast("Exception ${e.message}")
                } catch (e: Throwable) {
                    Log.d("check", "expection $e")

                    context.toast("Ooops: Something else went wrong$e")
                }
            }
        }
    }

    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


}