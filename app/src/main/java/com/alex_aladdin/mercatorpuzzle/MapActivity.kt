package com.alex_aladdin.mercatorpuzzle

import android.graphics.PixelFormat
import android.graphics.PointF
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

            country = createCountry()
            country!!.drawOnMap(mapboxMap)
            mySurfaceView.country = country
        }

        mySurfaceView.setZOrderMediaOverlay(true)               // Show MySurfaceView above MapView
        mySurfaceView.holder.setFormat(PixelFormat.TRANSPARENT) // Make MySurfaceView transparent
    }

    /**
     * Create test country.
     */
    private fun createCountry(): Country {
        val delta: Float = 100f
        val x: Float = mapView.width.toFloat() / 2
        val y: Float = mapView.height.toFloat() / 2

        val points1: List<PointF> = listOf(
                PointF(x + delta / 2, y - delta / 2),
                PointF(x + delta * 3/2, y - delta / 2),
                PointF(x + delta * 3/2, y + delta / 2),
                PointF(x + delta / 2, y + delta / 2)
        )

        val points2: List<PointF> = listOf(
                PointF(x - delta / 2, y - delta / 2),
                PointF(x - delta * 3/2, y - delta / 2),
                PointF(x - delta * 3/2, y + delta / 2),
                PointF(x - delta / 2, y + delta / 2)
        )

        return Country(
                vertices = arrayListOf(
                        ArrayList(points1.map { mapboxMap!!.projection.fromScreenLocation(it) }),
                        ArrayList(points2.map { mapboxMap!!.projection.fromScreenLocation(it) })
                ),
                id = "TEST",
                name = "Test Country")
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