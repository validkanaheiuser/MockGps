package com.lilstiffy.mockgps.service

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.lilstiffy.mockgps.models.LatLng
import com.lilstiffy.mockgps.MockGpsApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


object LocationHelper {
    private const val REQUEST_CODE = 69
    val DEFAULT_LOCATION = LatLng(40.712776, -74.005974)

    fun requestPermissions(activity: ComponentActivity) {
        activity.requestPermissions(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            ), REQUEST_CODE
        )
    }

    fun hasPermission(activity: ComponentActivity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Geocoding
    fun reverseGeocoding(latLng: LatLng, result: (Address?) -> Unit) {
        val geocoder = Geocoder(MockGpsApp.shared.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { response ->
                val address = response.firstOrNull()
                result(address)
            }
        } else {
            val response = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = response?.firstOrNull()
            result(address)
        }
    }

    /**
     * @param searchterm Search term the user wants to do a coordinate look up for
     * @param result lambda containing [LatLng] object if a result was found from the Geocoding lookup.
     */
    fun geocoding(searchterm: String, result: (LatLng?) -> Unit) {
        val geocoder = Geocoder(MockGpsApp.shared.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(
                searchterm,
                1
            ) { response ->
                val address = response.firstOrNull()
                if (address == null) {
                    result(null)
                    return@getFromLocationName
                }

                result(LatLng(address.latitude, address.longitude))
            }
        } else {
            val response = geocoder.getFromLocationName(searchterm, 1)
            val address = response?.firstOrNull()

            if (address == null) {
                result(null)
                return
            }

            result(LatLng(address.latitude, address.longitude))
        }
    }

    /**
     * Fetches the device's approximate location based on its public IP address.
     * Uses ip-api.com free geolocation API.
     * @return [LatLng] object with coordinates from IP geolocation, or null if failed.
     */
    suspend fun getLocationFromIp(): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://ip-api.com/json/?fields=status,lat,lon")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    if (json.getString("status") == "success") {
                        val lat = json.getDouble("lat")
                        val lon = json.getDouble("lon")
                        LatLng(lat, lon)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
