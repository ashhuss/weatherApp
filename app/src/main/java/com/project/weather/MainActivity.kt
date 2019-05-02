package com.project.weather

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
import android.widget.Toast
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), LocationListener {
    internal lateinit var locationManager: LocationManager
    private val REQUEST_LOCATION = 101
    private var lat: String? = null
    private var lng: String? = null
    private var location: Location? = null
    private lateinit var sharedPreferences: SharedPreferences
    val MY_DATA = "my_data"
    val lati = "lat"
    val lon = "lon"
    val unit = "unit"


    var connectionDetector: ConnectionDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        connectionDetector = ConnectionDetector(this)

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
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        getLastLocation()


        sharedPreferences = getSharedPreferences(MY_DATA, MODE_PRIVATE)
        val latDataCheck = sharedPreferences.getString(lati, "")
        val lonDataCheck = sharedPreferences.getString(lon, "")

        Log.d("latlngSpCheck", latDataCheck + "" + lonDataCheck)
        val service = RetrofitFactory.makeRetrofitService()
        CoroutineScope(Dispatchers.IO).launch {
            val request = service.getWeatherDataAsync(AppConstants.apiKey, latDataCheck, lonDataCheck,"imperial")
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    if (response.isSuccessful) {
                        //toast("Success")
                        val weatherList: WeatherModelList? = response.body()
                        val weatherDataModel = weatherList?.getWeatherList()
                        val weatherDataModelMain = weatherList?.mainData

                        //weather
                        current_weather.text = weatherDataModel?.get(0)?.main

                        // main
                        current_humitdity.text = weatherDataModelMain?.humidity.toString()

                    } else {
                        toast("Error: ${response.code()}")
                    }
                } catch (e: HttpException) {
                    toast("Exception ${e.message}")
                } catch (e: Throwable) {
                    Log.d("check", "expection $e")

                    toast("Ooops: Something else went wrong$e")
                }
            }
        }

    }

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

    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onLocationChanged(location: Location?) {
        //remove location callback:
        locationManager.removeUpdates(this)

        //getting latitude and longitude
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            toast("latitude:$latitude longitude:$longitude")

            lat = (latitude).toString()
            lng = (longitude).toString()

            save(lat!!, lng!!)

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
    private fun noInternetDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.no_internet_connection)
            .setMessage(R.string.internet_msg)
            .setPositiveButton(R.string.retry) { _, _ ->
                if (connectionDetector?.isConnected!!) {

                }
            }.setNegativeButton(
                R.string.close_all_caps
            ) { _, _ -> finish() }.setCancelable(false).show()
    }

    //Checks GPS status
    private fun buildAlertMessageNoGps() {
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


    //User's last location
    private fun getLastLocation() {
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

    internal fun save(latValue: String, lonValue: String) {
        sharedPreferences = getSharedPreferences(
            MY_DATA,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(lati, latValue)
        editor.putString(lon, lonValue)
        editor.commit()

    }
}

