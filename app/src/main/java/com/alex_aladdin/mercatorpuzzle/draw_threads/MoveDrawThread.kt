package com.alex_aladdin.mercatorpuzzle.draw_threads

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import com.alex_aladdin.mercatorpuzzle.activities.MapActivity
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Projection

/**
 * MoveDrawThread is used for a Country's movement drawing.
 */
class MoveDrawThread(surfaceHolder: SurfaceHolder,
                     val projection: Projection,
                     val country: Country) : DrawThread("MoveDrawThread", surfaceHolder) {

    companion object {

        const val ALPHA = 0.5f

    }

    @Volatile var touchPoint: PointF? = null
    @Volatile var stopDrawingInTargetPos = false
    private var arePolygonsRemoved = false

    override fun Canvas.drawFrame() {
        synchronized(country) {
            touchPoint?.let {
                val touchCoordinates: LatLng = projection.fromScreenLocation(it)
                country.currentCenter = touchCoordinates
            }
            this.drawCountry(
                    country = country.vertices,
                    projection = { latLng -> projection.toScreenLocation(latLng) },
                    color = country.color.setAlpha(ALPHA)
            )
            // Remove country's polygons from the map if they haven't been yet
            if (!arePolygonsRemoved) {
                arePolygonsRemoved = true
                Handler(Looper.getMainLooper()).post {
                    MapActivity.removePolygons(country)
                }
            }

            if (stopDrawingInTargetPos && country.currentCenter == country.targetCenter) {
                runFlag = false
            }
        }
    }

    /**
     * Set alpha to this color, where alpha is expressed by value in [0..1].
     */
    private fun Int.setAlpha(value: Float): Int {
        val alpha = (255 * value).toInt()
        val red = Color.red(this)
        val green = Color.green(this)
        val blue = Color.blue(this)
        return Color.argb(alpha, red, green, blue)
    }

}