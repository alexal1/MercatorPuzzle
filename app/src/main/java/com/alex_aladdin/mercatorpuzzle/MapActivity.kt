package com.alex_aladdin.mercatorpuzzle

import android.graphics.PixelFormat
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import com.alex_aladdin.mercatorpuzzle.country.CountriesDisposition
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.alex_aladdin.mercatorpuzzle.country.LatitudeBoundaries
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
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
    private var isMultiTouchAfterDrag = false

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

            if (MercatorApp.loadedCountries.isNotEmpty()) {
                onCountriesLoaded()
            }
            else {
                loadCountries()
            }
        }

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mySurfaceView.clearCanvas()
                    mySurfaceView.isEnabled = false
                    myFloatingActionButton.isFocusedOnCountry = false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mySurfaceView.isEnabled = true
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val isMultiTouchStarting = (event != null && event.actionMasked == MotionEvent.ACTION_POINTER_DOWN)
        val isMultiTouchGoing = (event != null && event.pointerCount > 1)
        val isMultiTouchFinishing = (event != null &&
                (event.actionMasked == MotionEvent.ACTION_POINTER_UP
                || event.action == MotionEvent.ACTION_UP
                || event.action == MotionEvent.ACTION_CANCEL))

        // Let mapView to handle multi touches that started immediately after country dragging
        if ((isMultiTouchGoing && isMultiTouchAfterDrag) || (isMultiTouchStarting && mySurfaceView.dragInProcess)) {
            if (mySurfaceView.dragInProcess) {
                val eventCopy = MotionEvent.obtain(event)
                eventCopy.action = MotionEvent.ACTION_CANCEL
                mySurfaceView.dispatchTouchEvent(eventCopy)
                mySurfaceView.clearCanvas()
                isMultiTouchAfterDrag = true
            }
            mapView.dispatchTouchEvent(event)
            return true
        }
        else {
            if (isMultiTouchFinishing) {
                isMultiTouchAfterDrag = false
            }
            return super.dispatchTouchEvent(event)
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
        else if (polygonsOnMap.containsKey(country)) {
            Log.e(TAG, "Cannot draw country, polygonsOnMap already contains one!")
            return
        }

        val countryPolygons = ArrayList<Polygon>()

        for (polygon in country.vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(TAG, "Incorrect polygon in ${country.name}!")
                continue
            }

            val polygonOptions = PolygonOptions()
                    .fillColor(country.color)
                    .addAll(polygon)
            val newPolygon = mapboxMap!!.addPolygon(polygonOptions)
            countryPolygons.add(newPolygon)
        }

        polygonsOnMap[country] = countryPolygons
    }

    /**
     * Move camera close to the given country.
     */
    fun focusCameraOn(country: Country) {
        val rect = country.getRect()
        val latLngBounds = LatLngBounds.Builder()
                .include(LatLng(rect.topLat, rect.rightLng))    // northeast
                .include(LatLng(rect.bottomLat, rect.leftLng))  // southwest
                .build()
        val padding = (maxOf(MercatorApp.screen.x, MercatorApp.screen.y) / 8).toInt()
        mapboxMap?.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding), 1000)
        mySurfaceView.countriesAnimator?.cancel()
        mySurfaceView.clearCanvas()
    }

    /**
     * Load countries from GeoJSON.
     */
    private fun loadCountries() {
        GeoJsonParser(completion = { countries ->
            MercatorApp.loadedCountries.addAll(countries)
            val viewPort = ViewPort(
                    northeast = mapboxMap!!.projection.fromScreenLocation(PointF(mapView.width.toFloat(), 0f)),
                    southwest = mapboxMap!!.projection.fromScreenLocation(PointF(0f, mapView.height.toFloat()))
            )
            CountriesDisposition(viewPort).apply(MercatorApp.loadedCountries)
            onCountriesLoaded()
        }).execute("RUS", "USA", "CHN", "LKA", "JPN")
    }

    /**
     * Function that's invoked when countries are loaded.
     */
    private fun onCountriesLoaded() {
        MercatorApp.shownCountries.forEach { removePolygons(it) }
        MercatorApp.shownCountries.clear()
        for (country in MercatorApp.loadedCountries) {
            country.color = MercatorApp.obtainColor()
            MercatorApp.shownCountries.add(country)
            country.addPropertyChangeListener(myFloatingActionButton)
        }
        myFloatingActionButton.currentCountry = MercatorApp.shownCountries.firstOrNull()
        mySurfaceView.showCountries(MercatorApp.shownCountries)
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

    /**
     * Representation of a certain area on the map.
     */
    data class ViewPort(val northeast: LatLng, val southwest: LatLng) {

        constructor() : this(
                northeast = LatLng(LatitudeBoundaries.MAX_MAP_LATITUDE, 180.0),
                southwest = LatLng(-LatitudeBoundaries.MAX_MAP_LATITUDE, -180.0)
        )

    }

}