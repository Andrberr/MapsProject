package com.example.mapsproject

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MainActivity : AppCompatActivity() {
    companion object {
        const val FIRST_MARKER =
            "https://abrakadabra.fun/uploads/posts/2022-01/1642606164_5-abrakadabra-fun-p-lokatsiya-ikonka-16.png"
        const val SECOND_MARKER =
            "https://abrakadabra.fun/uploads/posts/2022-01/1641345254_2-abrakadabra-fun-p-simvol-lokatsii-11.png"
    }


    private val mapView by lazy {
        findViewById<MapView>(R.id.mapView)
    }

    private var isAccess = false

    private val locationRequest = createLocationRequest()

    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Toast.makeText(
                        this@MainActivity,
                        (location.longitude.toString() + " " + location.latitude.toString()),
                        Toast.LENGTH_SHORT
                    ).show()

                    val point = Point(location.latitude, location.longitude)
                    putMarker(point, SECOND_MARKER)
                    stopLocationUpdates()
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
                isAccess = true
            }
            permissions.getOrDefault(
                ACCESS_COARSE_LOCATION,
                false
            ) -> {
                isAccess = true
            }
            else -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("460365c5-1066-4073-96c6-4089e0d7ce5a");
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        putMarker(Point(53.925157, 27.508873), FIRST_MARKER)

        locationPermissionRequest.launch(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        )
        findViewById<Button>(R.id.button).setOnClickListener {
            if (isAccess) startLocationUpdates()
        }
    }

    private fun putMarker(point: Point, url: String) {
        val markerSize =
            this@MainActivity.resources.getDimensionPixelSize(R.dimen.map_marker_icon_size)
        Glide.with(this@MainActivity).asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>(markerSize, markerSize) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val markerClickCallback =
                        MapObjectTapListener { _, _ ->
                            if (url == FIRST_MARKER) {
                                val bottomSheetFragment = OfficeInfoFragment()
                                bottomSheetFragment.show(
                                    supportFragmentManager,
                                    bottomSheetFragment.tag
                                )
                            }
                            true
                        }

                    mapView.map.mapObjects.addPlacemark(
                        point,
                        ImageProvider.fromBitmap(resource),
                        IconStyle()
                    ).addTapListener(markerClickCallback)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        mapView.getMap().move(
            CameraPosition(point, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )
    }

    private fun createLocationRequest(): LocationRequest {
        val timeInterval = 0L
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
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }
}