package com.alex_aladdin.mercatorpuzzle

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.PorterDuff
import android.util.Log
import android.view.SurfaceHolder
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Projection

/**
 * Thread for drawing on MySurfaceView.
 */
class DrawThread(val surfaceHolder: SurfaceHolder,
                 val projection: Projection,
                 val country: Country) : Thread() {

    private val LOG_TAG = "MercatorDrawThread"
    var runFlag: Boolean = false    // DrawThread is running at the moment
    var touchPoint: PointF? = null  // Point on the screen where user touches it

    override fun run() {
        var canvas: Canvas?

        Log.i(LOG_TAG, "Center: ${country.targetCenter}")

        // Start drawing
        while (runFlag) {
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas(null)
                synchronized (surfaceHolder) {
                    // Clear canvas
                    canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)

                    // Draw country
                    touchPoint?.let {
                        val touchCoordinates: LatLng = projection.fromScreenLocation(it)
                        country.updateVertices(newCenter = touchCoordinates)
                        country.drawOnCanvas(canvas!!, projection = { projection.toScreenLocation(it) })
                        // Remove country's polygons from the map if they haven't been yet
                        country.removeFromMap()
                    }
                }
            }
            finally {
                canvas?.let {
                    surfaceHolder.unlockCanvasAndPost(it)
                }
            }
        }
    }

}