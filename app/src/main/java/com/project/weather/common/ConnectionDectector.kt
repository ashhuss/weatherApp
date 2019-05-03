package com.project.weather.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionDetector(private val _context: Context) {

    val isConnected: Boolean
        get() {
            val connectivity = _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivity.allNetworkInfo
            if (info != null)
                for (i in info.indices)
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
            return false
        }
}