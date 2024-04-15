package com.example.taller2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taller2.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mSensorManager: SensorManager
    private lateinit var mLightSensor: Sensor
    private lateinit var mLightSensorEventListener: SensorEventListener
    private lateinit var currentLocation: Location
    private lateinit var locationManager: LocationManager
    private var geocoder: Geocoder? = null

    companion object {
        private const val REQUEST_CODE_LOCATION = 1
        private const val LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        private const val lowerLeftLatitude = 4.469031
        private const val lowerLeftLongitude = -74.1366813
        private const val upperRightLatitude = 4.8166886
        private const val upperRightLongitude = -74.0143209
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Sensors
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Light sensor
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        // Listener for light sensor
        mLightSensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                if (!::mMap.isInitialized || mMap == null) return // Verificar si mMap está inicializado y no es null

                val lightValue = event.values[0]
                if (lightValue > 10000) {
                    Log.i("MAPS", "LIGHT THEME\nLight value: $lightValue")
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.map_style_ligth))
                } else {
                    Log.i("MAPS", "DARK THEME\nLight value: $lightValue")
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.map_style_dark))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }



        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val txt = binding.etSearch.text.toString()
                if (txt.isNotEmpty() && geocoder != null) {
                    try {
                        val address = geocoder!!.getFromLocationName(
                            txt,
                            2,
                            lowerLeftLatitude,
                            lowerLeftLongitude,
                            upperRightLatitude,
                            upperRightLongitude
                        )

                        if (!address.isNullOrEmpty()) {
                            val locationAddress = address[0]
                            val markerLocation = Location("marker").apply {
                                latitude = locationAddress.latitude
                                longitude = locationAddress.longitude
                            }

                            if (::mMap.isInitialized) {
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(15F))
                                val markerLatLng = LatLng(markerLocation.latitude, markerLocation.longitude)
                                mMap.addMarker(
                                    MarkerOptions().position(markerLatLng)
                                        .title("Posicion geocoder")
                                        .snippet(txt)
                                        .alpha(1F)
                                )

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng))

                                // Calcular y mostrar la distancia
                                showDistanceToast(currentLocation, markerLocation)
                            }
                        } else {
                            Toast.makeText(this, "Ubicación inválida", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this, "Ubicación inválida", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No se proporcionó ninguna ubicación", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(mLightSensorEventListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(mLightSensorEventListener)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geocoder = Geocoder(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Habilitar la capa de mi ubicación en el mapa
        enableLocation()

        // Mover la cámara a la ubicación actual del usuario si está disponible
        if (isLocationPermissionGranted()) {
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                currentLocation = lastKnownLocation
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude), 15f))
            }
        }

        // Agregar el listener para el clic largo
        mMap.setOnMapLongClickListener { latLng ->
            mMap.clear() // Limpiar los marcadores existentes
            val location = Location("LongClickLocation")
            location.latitude = latLng.latitude
            location.longitude = latLng.longitude

            // Agregar un marcador en la ubicación del clic largo
            mMap.addMarker(MarkerOptions().position(latLng)
                .title("Marcador en $location")
                .snippet("Clic largo")
                .alpha(0.5F))

            // Calcular y mostrar la distancia entre la ubicación actual y el clic largo
            showDistanceToast(currentLocation, location)
        }

        // Agregar el listener para la actualización de la ubicación del usuario
        mMap.setOnMyLocationChangeListener { location ->
            currentLocation = location

            // Clear existing markers
            mMap.clear()

            // Add a marker at the new location
            val latLng = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(latLng)
                .title("Mi ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

            // Move the camera to the new location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        }
    }

    private fun getCurrentLocation(): Location {
        return if (ContextCompat.checkSelfPermission(
                this,
                LOCATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location ?: Location("default")
        } else {
            Location("default")
        }
    }

    private fun calculateDistance(currentLocation: Location, markerLocation: Location): Float {
        return currentLocation.distanceTo(markerLocation)
    }

    private fun showDistanceToast(currentLocation: Location, markerLocation: Location) {
        val distance = calculateDistance(currentLocation, markerLocation)
        val distanceString = String.format("%.2f", distance / 1000)
        Toast.makeText(this, "Distancia al marcador: $distanceString km", Toast.LENGTH_SHORT).show()
    }

    private fun isLocationPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (!::mMap.isInitialized) {
            return
        }
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(LOCATION_PERMISSION),
                    REQUEST_CODE_LOCATION
                )
                return
            }
            mMap.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(LOCATION_PERMISSION),
            REQUEST_CODE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mMap.isMyLocationEnabled = true
            } else -> {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
        }


    if (!::mMap.isInitialized) return
    if (!isLocationPermissionGranted()) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = false
        Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
    }
}


}
