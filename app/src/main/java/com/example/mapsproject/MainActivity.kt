package com.example.mapsproject

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView


class MainActivity : AppCompatActivity() {

    private val mapview  by lazy{
        findViewById<MapView>(R.id.mapView)
    }

    private val locationRequest = createLocationRequest()

    private val locationCallback: LocationCallback by lazy{
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Toast.makeText(this@MainActivity,(location.longitude.toString() + " " + location.latitude.toString()),Toast.LENGTH_SHORT).show()
                    mapview.getMap().move(
                        CameraPosition(Point(location.latitude, location.longitude), 11.0f, 0.0f, 0.0f),
                        Animation(Animation.Type.SMOOTH, 0F),
                        null
                    )
                }
            }
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(
                ACCESS_FINE_LOCATION,
                false
            ) -> {
                startLocationUpdates()//getLastLocation()
            }
            permissions.getOrDefault(
                ACCESS_COARSE_LOCATION,
                false
            ) -> {
                startLocationUpdates()//getLastLocation()
            }
            else -> {

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                println(location)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("460365c5-1066-4073-96c6-4089e0d7ce5a");
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        locationPermissionRequest.launch(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        )

        stopLocationUpdates()
    }

    private fun createLocationRequest(): LocationRequest {
        val timeInterval = 10000L
        return LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, timeInterval
        ).build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStop() {
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapview.onStart()
    }
}