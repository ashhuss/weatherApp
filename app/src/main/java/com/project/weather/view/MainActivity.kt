package com.project.weather.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.widget.Toast
import com.project.weather.R
import com.project.weather.common.AppConstants
import com.project.weather.common.ConnectionDetector
import com.project.weather.model.WeatherDataModel
import com.project.weather.presenter.WeatherImp
import com.project.weather.presenter.WeatherPresenter

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weather_forecast.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), LocationListener, WeatherView {


    private lateinit var locationManager: LocationManager
    private val REQUEST_LOCATION = 101
    private var lat: String? = null
    private var lng: String? = null
    private var location: Location? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val MY_DATA = "my_data"
    private val lati = "lat"
    private val lon = "lon"
    private var sdeg = "deg"
    private var spressure = "pressure"
    private var shumidity = "humidity"
    private var scountry = "country"
    private var smaxTemp = "maxTemp"
    private var sminTemp = "minTemp"
    private var scurrent = "current"
    private var scloudAll = "cloudall"
    private var sspeed = "speed"
    private var stemp = "temp"
    private var smain = "main"
    private var mainData: String = ""
    private var humidityData: String = ""
    private var maxTempData: String = ""
    private var minTempData: String = ""
    private var pressureData: String = ""
    private var speedData: String = ""
    private var degData: String = ""
    private var cloudAllData: String = ""
    private var countryData: String = ""
    private var currentData: String = ""
    private var tempData: String = ""
    private var latData: String = ""
    private var lonData: String = ""
    private var connectionDetector: ConnectionDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast)
        setSupportActionBar(toolbar)


        connectionDetector = ConnectionDetector(this)

        sharedPreferences = getSharedPreferences(MY_DATA, MODE_PRIVATE)
        settingValues()

        //Checking internet on device
        if (!connectionDetector!!.isConnected) {
            noInternetDialog()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Check Permissions
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
            )
            toast("Location permission not granted")

        } else {
            // permission has been granted, continue as usual
            toast("Location permission granted")
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        onLocationChanged(location)
        //last location
        getLastLocation()


        //Getting latLng value from shared preferences
        latData = sharedPreferences.getString(lati, "")
        lonData = sharedPreferences.getString(lon, "")



        toast(latData + " " + lonData)

        Log.d("ccc", "$latData $lonData")

        if (latData == "" && lonData == "") {
            //Checking GPS on device
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            }
        }

        val weatherPresenter: WeatherPresenter = WeatherImp(this, this)
        weatherPresenter.loadWeatherData(AppConstants.apiKey, latData, lonData, AppConstants.unit)


        //every two hours
        val scheduler = Executors.newSingleThreadScheduledExecutor()

        scheduler.scheduleAtFixedRate({
            // call service
            toast("Running")
            Log.d("running", "running")

        }, 0, 10, TimeUnit.SECONDS)

        //loading weather data

        tv_refresh.setOnClickListener {
            checkpoint()
        }
    }

    //getting response and attaching value after api call
    override fun showWeatherData(
        weatherModels: List<WeatherDataModel>?,
        weatherDataModelMain: WeatherDataModel,
        weatherDataModelWind: WeatherDataModel?,
        weatherDataModelSys: WeatherDataModel?,
        weatherDataModelCloud: WeatherDataModel?
    ) {
        //setting weather image
        when {
            weatherModels?.get(0)?.main.equals("Clear") -> {
                iv_weather.visibility = View.VISIBLE
                iv_weather.setBackgroundResource(R.drawable.ic_warm)
            }
            weatherModels?.get(0)?.main.equals("Rain") -> {
                iv_weather.visibility = View.VISIBLE
                iv_weather.setBackgroundResource(R.drawable.ic_rain)
            }
            weatherModels?.get(0)?.main.equals("Cloud") -> {
                iv_weather.visibility = View.VISIBLE
                iv_weather.setBackgroundResource(R.drawable.ic_partly_cloudy_blue)
            }
        }

        // RoundingUp the double values.
        tv_city.text = weatherDataModelSys?.country
        tv_current_status.text = weatherModels?.get(0)?.main
        val resultTemMax = Math.ceil(weatherDataModelMain?.tempMax!!).toInt()
        val resultTemMin = Math.ceil(weatherDataModelMain.tempMin).toInt()
        val resultTemp = Math.ceil(weatherDataModelMain.temp).toInt()
        val resultPressure = Math.ceil(weatherDataModelMain.pressure).toInt()
        val resultWind = Math.ceil(weatherDataModelWind!!.speed).toInt()
        val resultWindDeg = Math.ceil(weatherDataModelWind.deg).toInt()

        countryData = weatherDataModelSys?.country.toString()
        mainData = weatherModels?.get(0)?.main.toString()
        maxTempData = resultTemMax.toString()
        minTempData = resultTemMin.toString()
        pressureData = resultPressure.toString()
        speedData = resultWind.toString()
        degData = resultWindDeg.toString()
        tempData = resultTemp.toString()
        cloudAllData = weatherDataModelCloud?.all.toString()
        humidityData = weatherDataModelMain.humidity.toString()
        currentData = weatherModels?.get(0)?.main.toString()

        //Saving all data
        saveWeatherData(
            scountry,
            shumidity,
            smaxTemp,
            sminTemp,
            spressure,
            sspeed,
            sdeg,
            scloudAll,
            stemp,
            scurrent,
            smain
        )

        // Setting up weather forecast data to the views fro shared preferences'
        settingValues()

    }

    //Toast action
    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


    // Gets device location if the gps is on.
    override fun onLocationChanged(location: Location?) {
        //remove location callback:
        locationManager.removeUpdates(this)

        //getting latitude and longitude
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            toast("latitude:$latitude longitude:$longitude")

            if (latData == "" && lonData == "") {
                lat = (latitude).toString()
                lng = (longitude).toString()

                save(lat!!, lng!!)
            }
            Log.d("latlng", "latitude:$latitude longitude:$longitude")


        } else {
            toast("Lat Lng is null :(")
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    //No internet Dialog
    override fun noInternetDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.no_internet_connection)
            .setMessage(R.string.internet_msg)
            .setPositiveButton(R.string.retry) { _, _ ->
                checkpoint()
            }.setNegativeButton(
                R.string.close_all_caps
            ) { _, _ -> finish() }.setCancelable(false).show()
    }

    //Checks GPS status
    override fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Application requires location permission to retrieve weather forecast.")
            .setCancelable(false)
            .setPositiveButton(
                "Go to settings"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    //OnResume action
    override fun onResume() {
        super.onResume()
        Log.d("resume", "In the onResume() event")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0, 0f, this
        )
        onLocationChanged(location)
        getLastLocation()
    }

    //Device's last location
    override fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    //saving latLng value
    override fun save(latValue: String, lonValue: String) {
        sharedPreferences = getSharedPreferences(MY_DATA, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(lati, latValue)
        editor.putString(lon, lonValue)
        editor.apply()
    }

    //connection checkpoint
    override fun checkpoint() {
        if (connectionDetector?.isConnected!!) {
            val weatherPresenter: WeatherPresenter = WeatherImp(this, this)
            weatherPresenter.loadWeatherData(AppConstants.apiKey, latData, lonData, AppConstants.unit)
            toast("Data Loaded")
        } else {
            noInternetDialog()
            toast(getString(R.string.no_internet_connection))
        }
    }

    //setting up to save all data
    override fun saveWeatherData(
        country: String,
        humidity: String,
        maxTamp: String,
        minTemp: String,
        pressure: String,
        speed: String,
        deg: String,
        cloudAll: String,
        temp: String,
        current: String,
        main: String
    ) {
        sharedPreferences = getSharedPreferences(MY_DATA, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(scountry, country)
        editor.putString(shumidity, humidity)
        editor.putString(spressure, pressure)
        editor.putString(smaxTemp, maxTamp)
        editor.putString(sminTemp, minTemp)
        editor.putString(sspeed, speed)
        editor.putString(sdeg, deg)
        editor.putString(scurrent, current)
        editor.putString(stemp, temp)
        editor.putString(cloudAll, cloudAll)
        editor.putString(smain, cloudAll)
        editor.apply()
    }

    override fun settingValues() {
        smain = sharedPreferences.getString(smain, "")
        shumidity = sharedPreferences.getString(shumidity, "")
        smaxTemp = sharedPreferences.getString(smaxTemp, "")
        sminTemp = sharedPreferences.getString(sminTemp, "")
        spressure = sharedPreferences.getString(spressure, "")
        sspeed = sharedPreferences.getString(sspeed, "")
        sdeg = sharedPreferences.getString(sdeg, "")
        scloudAll = sharedPreferences.getString(scloudAll, "")
        scountry = sharedPreferences.getString(scountry, "")
        stemp = sharedPreferences.getString(stemp, "")
        Log.d("faa", humidityData + maxTempData + maxTempData + maxTempData + pressureData)

        tv_current_status.text = mainData
        tv_city.text = countryData
        tv_degree.text = degData
        tv_humidity.text = humidityData
        tv_temperature.text = tempData
        tv_pressure.text = pressureData
        tv_max_temperature.text = maxTempData
        tv_min_temperature.text = minTempData
        tv_clouds_all.text = cloudAllData
        tv_wind_speed.text = speedData
        tv_wind_deg.text = degData
        tv_current_status.text = currentData
    }

}

