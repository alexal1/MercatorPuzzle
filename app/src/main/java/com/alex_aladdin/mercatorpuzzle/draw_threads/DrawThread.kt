package com.alex_aladdin.mercatorpuzzle.draw_threads

import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import com.alex_aladdin.mercatorpuzzle.MercatorApp

/**
 * DrawThread is used to draw Countries on a SurfaceView using a background thread.
 */
abstract class DrawThread(threadName: String, private val surfaceHolder: SurfaceHolder) : Thread(threadName) {

    private val tag = "Mercator$threadName"
    private var runFlag: Boolean = false

    abstract fun Canvas.drawFrame()

    override fun run() {
        var canvas: Canvas?

        while (runFlag) {
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas(null)
                synchronized (surfaceHolder) {
                    canvas?.drawColor(0, PorterDuff.Mode.CLEAR)
                    canvas?.drawFrame()
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
     * Draw Country on the given Canvas as a list of polygons. Each polygon can be represented
     * by points of any type, which can be translated to PointF by given projection function.
     */
    protected fun <T> Canvas.drawCountry(country: List<List<T>>, projection: (T) -> PointF?, color: Int) {
        val paint = Paint()
        paint.color = color
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE

        // Draw one polygon on canvas
        fun drawPolygon(polygon: List<T>): Boolean {
            val path = Path()
            val pointStart: PointF = projection(polygon[0]) ?: return@drawPolygon false
            path.moveTo(pointStart.x, pointStart.y)

            // Part of polygon that got out to the opposite part of the screen
            val cutPolygon = ArrayList<T>()
            // This flag shows if we if we are going through cutPolygon's points or not
            var startCutPolygon = false
            // Previous point
            var prevPoint: PointF = pointStart
            // Half of screen width
            val halfScreen = MercatorApp.screen.x / 2
            // Go through all points
            (1 until polygon.size).forEach { i ->
                val point: PointF = projection(polygon[i]) ?: return@drawPolygon false

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
            return if (cutPolygon.isNotEmpty()) {
                drawPolygon(cutPolygon)
            }
            else {
                true
            }
        }

        for (polygon in country) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(tag, "Incorrect polygon!")
                continue
            }

            val isPolygonDrawn = drawPolygon(polygon)
            if (!isPolygonDrawn) break
        }
    }

    /**
     * Euclidean distance between two points.
     */
    private fun PointF.distanceTo(point: PointF): Float {
        return Math.sqrt(Math.pow(point.x.toDouble() - this.x.toDouble(), 2.0)
                + Math.pow(point.y.toDouble() - this.y.toDouble(), 2.0)).toFloat()
    }

    /**
     * Starts this DrawThread.
     */
    fun startDrawing() {
        runFlag = true
        this@DrawThread.start()
    }

    /**
     * Stops this DrawThread.
     */
    fun stopDrawing() {
        var retry = true
        runFlag = false
        while (retry) {
            try {
                join()
                retry = false
            }
            catch (e: InterruptedException) {
                Log.e(tag, e.toString())
            }
        }
    }

}