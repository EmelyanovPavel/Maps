package com.example.maps

//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//
//
//class MainActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var map: GoogleMap
//
//    // Координаты Северодвинска
//    private val severodvinsk = LatLng(64.5609, 39.8139)
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Получаем фрагмент карты
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        map = googleMap
//
//        // Устанавливаем камеру на Северодвинск
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(severodvinsk, 12f))
//
//        // Добавляем маркер (опционально)
//        map.addMarker(
//            com.google.android.gms.maps.model.MarkerOptions()
//                .position(severodvinsk)
//                .title("Северодвинск")
//        )
//    }
//}
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), GoogleMap.OnMapClickListener {

    private lateinit var googleMap: GoogleMap
    private val markers = mutableListOf<Marker>()

    lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geocoder = Geocoder(this, Locale.getDefault())

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(64.5609, 39.8139),
                    12f
                )
            ) // Северодвинск

            googleMap.setOnMapClickListener(this)
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapClick(latLng: LatLng) {
// Удаление маркера
        val markerToRemove = markers.find {
            val position = it.position
            val threshold = 0.0005 // погрешность
            kotlin.math.abs(position.latitude - latLng.latitude) < threshold &&
                    kotlin.math.abs(position.longitude - latLng.longitude) < threshold
        }
        if (markerToRemove != null) {
            markerToRemove.remove()
            markers.remove(markerToRemove)
        } else {
            // Иначе добавляем маркер
            getCityNameFromLatLng(latLng, geocoder) ?: "Unknown place"

        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getCityNameFromLatLng(latLng: LatLng, geocoder: Geocoder) {
        geocoder.getFromLocation(
            latLng.latitude, latLng.longitude, 3, @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            object : Geocoder.GeocodeListener {

                override fun onError(errorMessage: String?) {
                    Log.e("Geocoding", "Error: $errorMessage")
                }

                override fun onGeocode(addresses: List<Address?>) {
                    var address = "Address was not found."
                    if (!addresses.isEmpty()) {
                        val firstAddress = addresses.first()
                        address = "City: ${firstAddress?.locality}"
                    }
                    Log.d("Geocoding", address)

                    /*runOnUiThread(object : Runnable {
                        override fun run() {
                            val marker = googleMap.addMarker(
                                MarkerOptions().position(latLng)
                                    .title(address)
                            )

                            val marker2 = null
                            marker?.let {
                                markers.add(it)
                            }
                        }
                    })
                      })
    }*/

                    lifecycleScope.launch {
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng)
                                .title(address)
                        )
                        marker?.let { markers.add(it) }
                    }
                }
            })

    }

}