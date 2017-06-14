package com.alex_aladdin.mercatorpuzzle

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.commons.models.Position
import com.mapbox.services.commons.turf.TurfJoins

/**
 * Custom SurfaceView that's used to draw polygons.
 */
class MySurfaceView : SurfaceView, SurfaceHolder.Callback {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val LOG_TAG = "MercatorMySurfaceView"
    var mapboxMap: MapboxMap? = null
    var polygon: Polygon? = null
    var drawThread: DrawThread? = null
    var dragInProcess: Boolean = false

    init {
        // Get our SurfaceHolder object and tell him that we wanna receive callbacks
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDrawThread()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            // Check out is touch inside polygon or not
            if (mapboxMap == null || polygon == null) {
                return false
            }
            else {
                val touchPoint: PointF = PointF(event.x, event.y)
                val touchCoordinates: LatLng = mapboxMap!!.projection.fromScreenLocation(touchPoint)

                val polygonPositions: ArrayList<Position> = arrayListOf()
                polygon!!.points.mapTo(polygonPositions, { point ->
                    Position.fromCoordinates(point.longitude, point.latitude)
                })

                val isInside: Boolean = TurfJoins.inside(Position.fromCoordinates(
                        touchCoordinates.longitude,
                        touchCoordinates.latitude
                ), polygonPositions)

                // If touch is inside polygon, start dragging
                if (isInside)
                    startDrawThread()
                dragInProcess = isInside
                Log.i(LOG_TAG, "Touch is inside polygon: $isInside")

                return isInside
            }
        }
        else if (event?.action == MotionEvent.ACTION_MOVE) {
            if (dragInProcess) {
                drawThread?.touchPoint = PointF(event.x, event.y)
                return true
            }
            else
                return false
        }
        else if (event?.action == MotionEvent.ACTION_UP) {
            if (dragInProcess) {
                dragInProcess = false
                stopDrawThread()
                return true
            }
            else
                return false
        }
        else
            return super.onTouchEvent(event)
    }

    /**
     * Start background thread that performs drawing on this SurfaceView.
     */
    private fun startDrawThread() {
        if (mapboxMap != null && polygon != null) {
            drawThread = DrawThread(holder, mapboxMap!!, polygon!!)
            drawThread!!.runFlag = true
            drawThread!!.start()
        }
    }

    /**
     * Stop background thread that performs drawing on this SurfaceView.
     */
    private fun stopDrawThread() {
        var retry: Boolean = true
        drawThread?.apply {
            runFlag = false
            while (retry) {
                try {
                    join()
                    retry = false
                }
                catch (e: InterruptedException) {
                    Log.e(LOG_TAG, e.toString())
                }
            }
        }
    }

}