package com.alex_aladdin.mercatorpuzzle

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {

    var mapboxMap: MapboxMap? = null
    var polygon: Polygon? = null

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

            // Configure appearance
            mapboxMap.uiSettings.isRotateGesturesEnabled = false
            mapboxMap.uiSettings.isCompassEnabled = false
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false

            polygon = drawPolygon()
        }
    }

    /**
     *  Draw square on the map at the center of the screen.
     */
    private fun drawPolygon(): Polygon {
        val delta: Float = 100f
        val x: Float = mapView.width.toFloat() / 2
        val y: Float = mapView.height.toFloat() / 2

        val points: List<PointF> = listOf(
                PointF(x - delta, y - delta),
                PointF(x + delta, y - delta),
                PointF(x + delta, y + delta),
                PointF(x - delta, y + delta)
        )

        val projection = mapboxMap!!.projection
        val coordinates: List<LatLng> = points.map { projection.fromScreenLocation(it) }

        val square = mapboxMap!!.addPolygon(PolygonOptions()
                .addAll(coordinates)
                .fillColor(Color.parseColor("#ff0000")))

        return square
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