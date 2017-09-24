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

    companion object {

        const val TAG = "MercatorMySurfaceView"

    }

    var mapboxMap: MapboxMap? = null
    var country: Country? = null

    private var drawThread: DrawThread? = null
    private var countryAnimator: CountryAnimator? = null
    private var dragInProcess: Boolean = false

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
        if (countryAnimator?.isInProgress == true) {
            return true
        }

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check out if touch is inside country or not
                if (mapboxMap == null || country == null) {
                    return false
                }
                else {
                    val touchPoint = PointF(event.x, event.y)
                    val touchCoordinates: LatLng = mapboxMap!!.projection.fromScreenLocation(touchPoint)
                    val isInside = country!!.contains(touchCoordinates)

                    // If touch is inside country, start dragging
                    if (isInside) {
                        startDrawThread()
                    }
                    dragInProcess = isInside
                    Log.i(TAG, "Touch is inside country: $isInside")

                    return isInside
                }
            }

            MotionEvent.ACTION_MOVE -> {
                return if (dragInProcess) {
                    drawThread?.touchPoint = PointF(event.x, event.y)
                    true
                }
                else {
                    false
                }
            }

            MotionEvent.ACTION_UP -> {
                if (dragInProcess) {
                    dragInProcess = false

                    // Operations to do either immediately or after animation finishes
                    fun doFinally() {
                        stopDrawThread()
                        mapboxMap?.let {
                            (context as MapActivity).drawCountry()
                        }
                    }

                    if (country?.isCloseToTarget() == true) {
                        drawThread?.let { countryAnimator = CountryAnimator(it) }
                        countryAnimator?.animate { doFinally() }
                    }
                    else {
                        doFinally()
                    }

                    return true
                }
                else {
                    return false
                }
            }

            else -> return super.onTouchEvent(event)
        }
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
        var retry = true
        drawThread?.apply {
            runFlag = false
            while (retry) {
                try {
                    join()
                    retry = false
                }
                catch (e: InterruptedException) {
                    Log.e(TAG, e.toString())
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