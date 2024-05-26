package com.example.gpstracking

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LocationData(
    @SerializedName("vehicle_id") val vehicleId: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: String
)

interface ApiService {
    @POST("api/locations")
    suspend fun sendLocation(@Body location: LocationData)
}

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    sendLocationToServer(location)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun sendLocationToServer(location: Location) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://yourserver.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val locationData = LocationData(
            vehicleId = "1234",
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = System.currentTimeMillis().toString()
        )

        try {
            apiService.sendLocation(locationData)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to send location", Toast.LENGTH_SHORT).show()
        }
    }
}
