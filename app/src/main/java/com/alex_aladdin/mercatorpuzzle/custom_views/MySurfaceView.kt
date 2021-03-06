package com.alex_aladdin.mercatorpuzzle.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.alex_aladdin.google_maps_utils.PolyUtil
import com.alex_aladdin.mercatorpuzzle.activities.MapActivity
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.animators.CountriesAnimator
import com.alex_aladdin.mercatorpuzzle.animators.MoveCountriesAnimator
import com.alex_aladdin.mercatorpuzzle.animators.ScaleCountriesAnimator
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.alex_aladdin.mercatorpuzzle.draw_threads.DrawThread
import com.alex_aladdin.mercatorpuzzle.draw_threads.MoveDrawThread
import com.alex_aladdin.mercatorpuzzle.draw_threads.ScaleDrawThread
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
    var dragInProcess: Boolean = false
    var countriesAnimator: CountriesAnimator? = null

    private val halfTouchSide = resources.getDimension(R.dimen.my_surface_view_touch_side) / 2
    private var drawThread: DrawThread? = null
    private var currentCountry: Country? = null

    init {
        // Get our SurfaceHolder object and tell him that we wanna receive callbacks
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        drawThread?.stopDrawing()
        drawThread = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }

        countriesAnimator?.cancel()

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check out whether some country is touched
                val touchPoint = PointF(event.x, event.y)
                currentCountry = findTouchedCountry(touchPoint)

                if (currentCountry != null) {
                    val country = currentCountry!!

                    drawThread?.stopDrawing()
                    drawThread = MoveDrawThread(
                            surfaceHolder = holder,
                            projection = mapboxMap!!.projection,
                            country = country
                    )
                    drawThread!!.startDrawing()
                    dragInProcess = true
                    Log.i(TAG, "Touch is inside ${country.name}")

                    // Move this country to the first position in the array
                    if (MercatorApp.shownCountries[0] != country) {
                        MercatorApp.shownCountries.remove(country)
                        MercatorApp.shownCountries.add(0, country)
                    }

                    return true
                }
                else {
                    return false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                return if (dragInProcess) {
                    (drawThread as? MoveDrawThread)?.touchPoint = PointF(event.x, event.y)
                    true
                }
                else {
                    false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragInProcess) {
                    dragInProcess = false

                    // Operations to do either immediately or after animation finishes
                    fun doFinally() {
                        currentCountry?.let { country ->
                            Handler(Looper.getMainLooper()).post {
                                if (!country.isFixed) {
                                    (context as MapActivity).drawCountry(country)
                                }
                                else {
                                    val countriesToRedraw = MercatorApp.shownCountries
                                            .filter { !it.isFixed && it.intersects(country) }
                                            .plus(country)
                                    (context as MapActivity).redrawCountries(countriesToRedraw)
                                    clearCanvas()
                                }
                            }
                        }
                    }

                    if (event.action == MotionEvent.ACTION_UP && currentCountry?.isCloseToTarget() == true) {
                        (drawThread as? MoveDrawThread)?.let { moveDrawThread ->
                            countriesAnimator = MoveCountriesAnimator(moveDrawThread)
                            countriesAnimator?.animate {
                                moveDrawThread.stopDrawingInTargetPos = true
                                drawThread = null
                                currentCountry?.isFixed = true
                                doFinally()
                            }
                        }
                    }
                    else {
                        drawThread?.stopDrawing()
                        drawThread = null
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

    fun showCountries(countries: List<Country>) {
        if (mapboxMap == null) {
            Log.e(TAG, "Cannot show countries, mapboxMap is null!")
            return
        }

        if (!isEnabled) {
            countries.forEach {
                (context as MapActivity).drawCountry(it)
            }
            return
        }

        drawThread?.stopDrawing()
        drawThread = ScaleDrawThread.obtain(
                surfaceHolder = holder,
                projection = mapboxMap!!.projection,
                countries = countries
        )
        drawThread!!.startDrawing()

        countriesAnimator = ScaleCountriesAnimator(drawThread as ScaleDrawThread)
        countriesAnimator!!.animate {
            drawThread?.stopDrawing()
            drawThread = null
            countries.forEach {
                (context as MapActivity).drawCountry(it)
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

    private fun findTouchedCountry(touchPoint: PointF): Country? {
        if (mapboxMap == null) {
            Log.e(TAG, "Cannot find touched Country as mapboxMap is null")
            return null
        }

        // Touch point
        val touchLatLng = mapboxMap!!.projection.fromScreenLocation(touchPoint)

        // Touch square
        val squareByPoint: List<PointF> by lazy {
            List(size = 4, init = { i ->
                when (i) {
                    0 -> PointF(touchPoint.x - halfTouchSide, touchPoint.y + halfTouchSide)
                    1 -> PointF(touchPoint.x + halfTouchSide, touchPoint.y + halfTouchSide)
                    2 -> PointF(touchPoint.x + halfTouchSide, touchPoint.y - halfTouchSide)
                    3 -> PointF(touchPoint.x - halfTouchSide, touchPoint.y - halfTouchSide)
                    else -> throw IllegalArgumentException()
                }
            })
        }
        val squareByLatLng: List<LatLng> by lazy {
            List(size = 4, init = { i ->
                mapboxMap!!.projection.fromScreenLocation(squareByPoint[i])
            })
        }

        for (country in MercatorApp.shownCountries) {
            if (country.isFixed) {
                continue
            }

            if (country.contains(touchLatLng)) {
                return@findTouchedCountry country
            }

            country.vertices.flatten().firstOrNull { vertex ->
                PolyUtil.containsLocation(vertex, squareByLatLng, false)
            }?.let { return@findTouchedCountry country }
        }

        return null
    }

}