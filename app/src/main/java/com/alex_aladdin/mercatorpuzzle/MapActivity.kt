package com.alex_aladdin.mercatorpuzzle

import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {

    companion object {

        val TAG = "MercatorMapActivity"

        private val polygonsOnMap: ArrayList<Polygon> = ArrayList()

        /**
         * Remove country from the map.
         */
        fun removePolygons() {
            if (polygonsOnMap.isNotEmpty()) {
                polygonsOnMap.forEach { it.remove() }
                polygonsOnMap.clear()
            }
        }

    }

    private var mapboxMap: MapboxMap? = null
    private var country: Country? = null

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

            mapboxMap.setOnCameraMoveStartedistener {
                mySurfaceView.clearCanvas()
                mySurfaceView.isEnabled = false
            }

            mapboxMap.setOnCameraIdleListener {
                mySurfaceView.isEnabled = true
            }

            addCountry()
        }

        mySurfaceView.setZOrderMediaOverlay(true)               // Show MySurfaceView above MapView
        mySurfaceView.holder.setFormat(PixelFormat.TRANSPARENT) // Make MySurfaceView transparent
    }

    /**
     * Draw country on the map.
     */
    fun drawCountry() {
        if (country == null) {
            Log.e(TAG, "Cannot draw country, country is null!")
            return
        }

        if (mapboxMap == null) {
            Log.e(TAG, "Cannot draw country, mapboxMap is null!")
            return
        }

        for (polygon in country!!.vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(TAG, "Incorrect polygon in ${country!!.name}!")
                continue
            }

            val polygonOptions = PolygonOptions()
                    .fillColor(Color.parseColor("#ff0000"))
                    .addAll(polygon)
            val newPolygon = mapboxMap!!.addPolygon(polygonOptions)
            polygonsOnMap.add(newPolygon)
        }
    }

    /**
     * Add country to the map.
     */
    private fun addCountry() {
        GeoJsonParser(completion = { countries ->
            country = countries.first()
            drawCountry()
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