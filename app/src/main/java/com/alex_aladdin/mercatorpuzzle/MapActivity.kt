package com.alex_aladdin.mercatorpuzzle

import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {

    companion object {

        val TAG = "MercatorMapActivity"

        private val polygonsOnMap = HashMap<Country, ArrayList<Polygon>>()

        /**
         * Remove country from the map.
         */
        fun removePolygons(country: Country) {
            polygonsOnMap[country]?.forEach { polygon -> polygon.remove() }
            polygonsOnMap[country]?.clear()
            polygonsOnMap.remove(country)
        }

    }

    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_map)

        mapView.onCreate(savedInstanceState)
        initMap()

        mySurfaceView.setZOrderMediaOverlay(true)               // Show MySurfaceView above MapView
        mySurfaceView.holder.setFormat(PixelFormat.TRANSPARENT) // Make MySurfaceView transparent
    }

    /**
     * All initial operations with the map.
     */
    private fun initMap() {
        mapView.getMapAsync { mapboxMap ->
            // Save object
            this.mapboxMap = mapboxMap
            mySurfaceView.mapboxMap = mapboxMap

            // Configure appearance
            mapboxMap.uiSettings.isRotateGesturesEnabled = false
            mapboxMap.uiSettings.isCompassEnabled = false
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false

            addCountries()
        }

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mySurfaceView.clearCanvas()
                    mySurfaceView.isEnabled = false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mySurfaceView.isEnabled = true
                }
            }
            return@setOnTouchListener false
        }
    }

    /**
     * Draw country on the map.
     */
    fun drawCountry(country: Country) {
        if (mapboxMap == null) {
            Log.e(TAG, "Cannot draw country, mapboxMap is null!")
            return
        }

        polygonsOnMap[country] = ArrayList()

        for (polygon in country.vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(TAG, "Incorrect polygon in ${country.name}!")
                continue
            }

            val polygonOptions = PolygonOptions()
                    .fillColor(Color.parseColor("#ff0000"))
                    .addAll(polygon)
            val newPolygon = mapboxMap!!.addPolygon(polygonOptions)
            polygonsOnMap[country]?.add(newPolygon)
        }
    }

    /**
     * Add countries to the map.
     */
    private fun addCountries() {
        GeoJsonParser(completion = { countries ->
            MercatorApp.loadedCountries.addAll(countries)
            MercatorApp.loadedCountries.forEach { drawCountry(it) }
        }).execute("RUS", "USA", "CHN", "LKA", "JPN")
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