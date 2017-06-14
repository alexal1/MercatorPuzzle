package com.alex_aladdin.mercatorpuzzle

import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * Thread for drawing on MySurfaceView.
 */
class DrawThread(val surfaceHolder: SurfaceHolder,
                 val mapboxMap: MapboxMap,
                 val polygon: Polygon) : Thread() {

    private val LOG_TAG = "MercatorDrawThread"
    var runFlag: Boolean = false    // DrawThread is running at the moment
    var touchPoint: PointF? = null  // Point on the screen where user touches it

    override fun run() {
        val centerCoordinates: LatLng = polygon.center()
        var util: SphericalUtil
        var canvas: Canvas?

        // Start drawing
        while (runFlag) {
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas(null)
                synchronized (surfaceHolder) {
                    // Clear canvas
                    canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)

                    // Draw rectangle
                    val paint = Paint()
                    paint.color = Color.RED
                    touchPoint?.let {
                        val touchCoordinates: LatLng = mapboxMap.projection.fromScreenLocation(it)
                        Log.i(LOG_TAG, "Touch coordinates = $touchCoordinates")

                        util = SphericalUtil(from = centerCoordinates, to = touchCoordinates)
                        val newPoints: ArrayList<PointF> = ArrayList(polygon.points
                                .map { util.getNewCoordinates(it) }
                                .map { mapboxMap.projection.toScreenLocation(it) })

                        // Draw polygon by newPoints array
                        val path: Path = Path()
                        path.moveTo(newPoints[0].x, newPoints[0].y)
                        newPoints.removeAt(0)
                        for (point in newPoints) {
                            path.lineTo(point.x, point.y)
                        }
                        canvas!!.drawPath(path, paint)
                    }
                }
            }
            finally {
                canvas?.let {
                    surfaceHolder.unlockCanvasAndPost(it)
                }
            }
        }

        // Finally clear canvas
        clearCanvas()
    }

    /**
     * Just clear canvas.
     */
    private fun clearCanvas() {
        var canvas: Canvas? = null
        try {
            canvas = surfaceHolder.lockCanvas(null)
            synchronized (surfaceHolder) {
                canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
            }
        }
        finally {
            canvas?.let {
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }
    }

    /**
     * Get center of polygon.
     */
    private fun Polygon.center(): LatLng {
        val points = this.points
        val latitudes = points.map { it.latitude }.sorted()
        val longitudes = points.map { it.longitude }.sorted()
        val minLat: Double = latitudes.first()
        val maxLat: Double = latitudes.last()
        val minLng: Double = longitudes.first()
        val maxLng: Double = longitudes.last()
        return LatLng((minLat + maxLat) / 2.0, (minLng + maxLng) / 2.0)
    }

}