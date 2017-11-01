package com.alex_aladdin.mercatorpuzzle.draw_threads

import android.graphics.Canvas
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.alex_aladdin.mercatorpuzzle.MapActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Projection

/**
 * MoveDrawThread is used for a Country's movement drawing.
 */
class MoveDrawThread(surfaceHolder: SurfaceHolder,
                     val projection: Projection,
                     val country: Country) : DrawThread("MoveDrawThread", surfaceHolder) {

    var touchPoint: PointF? = null
    private var arePolygonsRemoved = false

    override fun Canvas.drawFrame() {
        touchPoint?.let {
            val touchCoordinates: LatLng = projection.fromScreenLocation(it)
            country.currentCenter = touchCoordinates
            this.drawCountry(
                    country = country.vertices,
                    projection = { latLng -> projection.toScreenLocation(latLng) }
            )
            // Remove country's polygons from the map if they haven't been yet
            if (!arePolygonsRemoved) {
                arePolygonsRemoved = true
                Handler(Looper.getMainLooper()).post {
                    MapActivity.removePolygons(country)
                }
            }
        }
    }

}