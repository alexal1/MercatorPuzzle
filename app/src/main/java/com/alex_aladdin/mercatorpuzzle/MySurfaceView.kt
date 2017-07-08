package com.alex_aladdin.mercatorpuzzle

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * Custom SurfaceView that's used to draw polygons.
 */
class MySurfaceView : SurfaceView, SurfaceHolder.Callback {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val LOG_TAG = "MercatorMySurfaceView"
    var mapboxMap: MapboxMap? = null
    var country: Country? = null
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
            // Check out if touch is inside country or not
            if (mapboxMap == null || country == null) {
                return false
            }
            else {
                val touchPoint: PointF = PointF(event.x, event.y)
                val touchCoordinates: LatLng = mapboxMap!!.projection.fromScreenLocation(touchPoint)
                val isInside = country!!.contains(touchCoordinates)

                // If touch is inside country, start dragging
                if (isInside) {
                    startDrawThread()
                }
                dragInProcess = isInside
                Log.i(LOG_TAG, "Touch is inside country: $isInside")

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
                mapboxMap?.let {
                    country?.drawOnMap(it)
                }
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
        if (mapboxMap != null && country != null) {
            drawThread = DrawThread(holder, mapboxMap!!.projection, country!!)
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

    /**
     * Just clear canvas.
     */
    fun clearCanvas() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas(null)
            synchronized (holder) {
                canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
            }
        }
        finally {
            canvas?.let {
                holder.unlockCanvasAndPost(it)
            }
        }
    }

}