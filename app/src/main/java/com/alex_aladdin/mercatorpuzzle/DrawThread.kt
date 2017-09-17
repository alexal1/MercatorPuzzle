package com.alex_aladdin.mercatorpuzzle

import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Projection

/**
 * Thread for drawing on MySurfaceView.
 */
class DrawThread(val surfaceHolder: SurfaceHolder,
                 val projection: Projection,
                 val country: Country) : Thread("DrawThread") {

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
                    canvas?.drawColor(0, PorterDuff.Mode.CLEAR)

                    // Draw country
                    touchPoint?.let {
                        val touchCoordinates: LatLng = projection.fromScreenLocation(it)
                        country.currentCenter = touchCoordinates
                        canvas?.drawCountry()
                        // Remove country's polygons from the map if they haven't been yet
                        MapActivity.removePolygons()
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

    /**
     * Draw country on given Canvas.
     */
    private fun Canvas.drawCountry() {
        val paint = Paint()
        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE

        // Draw one polygon on canvas
        fun drawPolygon(polygon: ArrayList<LatLng>) {
            val path = Path()
            val pointStart: PointF = projection.toScreenLocation(polygon[0])
            path.moveTo(pointStart.x, pointStart.y)

            // Part of polygon that got out to the opposite part of the screen
            val cutPolygon = ArrayList<LatLng>()
            // This flag shows if we if we are going through cutPolygon's points or not
            var startCutPolygon = false
            // Previous point
            var prevPoint: PointF = pointStart
            // Half of screen width
            val halfScreen = MercatorApp.screen.x / 2
            // Go through all points
            (1..polygon.size-1).forEach { i ->
                val point: PointF = projection.toScreenLocation(polygon[i])

                if (point.distanceTo(prevPoint) > halfScreen) {
                    startCutPolygon = !startCutPolygon
                }

                if (startCutPolygon) {
                    cutPolygon.add(polygon[i])
                }
                else {
                    path.lineTo(point.x, point.y)
                }

                prevPoint = point
            }

            path.close()
            this@drawCountry.drawPath(path, paint)

            // Recursive call to draw cutPolygon
            if (cutPolygon.isNotEmpty()) {
                drawPolygon(cutPolygon)
            }
        }

        for (polygon in country.vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(LOG_TAG, "Incorrect polygon in ${country.name}!")
                continue
            }

            drawPolygon(polygon)
        }
    }

    /**
     * Euclidean distance between two points.
     */
    private fun PointF.distanceTo(point: PointF): Float {
        return Math.sqrt(Math.pow(point.x.toDouble() - this.x.toDouble(), 2.0)
                + Math.pow(point.y.toDouble() - this.y.toDouble(), 2.0)).toFloat()
    }

}