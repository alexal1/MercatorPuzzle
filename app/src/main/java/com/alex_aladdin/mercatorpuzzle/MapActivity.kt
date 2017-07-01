package com.alex_aladdin.mercatorpuzzle

import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {

    var mapboxMap: MapboxMap? = null
    var country: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get Mapbox instance
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_map)

        // Create a mapView
        mapView.onCreate(savedInstanceState)
        // Add a MapboxMap
        mapView.getMapAsync { mapboxMap ->
            // Save object
            this.mapboxMap = mapboxMap
            mySurfaceView.mapboxMap = mapboxMap

            // Configure appearance
            mapboxMap.uiSettings.isRotateGesturesEnabled = false
            mapboxMap.uiSettings.isCompassEnabled = false
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false

            addCountry()
        }

        mySurfaceView.setZOrderMediaOverlay(true)               // Show MySurfaceView above MapView
        mySurfaceView.holder.setFormat(PixelFormat.TRANSPARENT) // Make MySurfaceView transparent
    }

    /**
     * Add country to the map.
     */
    private fun addCountry() {
        GeoJsonParser(completion = { countries ->
            country = countries.first()
            country!!.drawOnMap(mapboxMap!!)
            mySurfaceView.country = country
        }).execute("RUS")
    }

    public override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

}