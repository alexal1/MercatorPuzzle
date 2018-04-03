package com.alex_aladdin.mercatorpuzzle.custom_views

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.zoom_view.view.*

class ZoomView : LinearLayout {

    companion object {

        const val TAG = "MercatorZoomView"

    }

    enum class Zoom { IN, OUT, NO }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.zoom_view_button_background)
    private val defaultColor = ContextCompat.getColor(context, R.color.country_fixed)
    private val screenRect = RectF(0f, 0f, MercatorApp.screen.x, MercatorApp.screen.y)
    private var buttonArrow: ImageButton? = null
    private var currentAngle = 0.0
    var mapboxMap: MapboxMap? = null
    var currentCountry: Country? = null
        set(value) {
            if (value == field) return

            field = value

            if (value != null) {
                setColor(value.color)
            }
            else {
                setColor(defaultColor)
            }

            onMapChanged()
        }
    var onButtonClick: (zoom: Zoom) -> Unit = {}

    init {
        inflate(context, R.layout.zoom_view, this@ZoomView)
        this@ZoomView.orientation = LinearLayout.VERTICAL
        this@ZoomView.setBackgroundResource(R.drawable.zoom_view_background)
        this@ZoomView.layoutTransition = LayoutTransition()
        buttonPlus.background = backgroundDrawable
        buttonMinus.background = backgroundDrawable
        setColor(defaultColor)
        setListeners()
    }

    private fun setListeners() {
        buttonPlus.setOnClickListener {
            onButtonClick(Zoom.IN)
        }
        buttonMinus.setOnClickListener {
            onButtonClick(Zoom.OUT)
        }
    }

    private fun setColor(color: Int) {
        backgroundDrawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        buttonPlus.invalidate()
        buttonMinus.invalidate()
        buttonArrow?.invalidate()
    }

    private fun addButtonArrow() {
        if (buttonArrow != null) {
            return
        }

        // Create view
        val view = ImageButton(context)
        val size = resources.getDimension(R.dimen.zoom_view_button_size).toInt()
        view.layoutParams = LinearLayout.LayoutParams(size, size)
        val margin = resources.getDimension(R.dimen.zoom_view_button_margin).toInt()
        (view.layoutParams as MarginLayoutParams).setMargins(margin, margin, margin, 0)
        view.background = backgroundDrawable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.foreground = ContextCompat.getDrawable(context, R.drawable.zoom_view_button_foreground)
        }
        view.setImageResource(R.drawable.arrow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.translationZ = resources.getDimension(R.dimen.zoom_view_button_translation_z)
        }
        view.setOnClickListener {
            onButtonClick(Zoom.NO)
        }

        // Add view to the layout
        this@ZoomView.addView(view, 0)

        buttonArrow = view
    }

    private fun removeButtonArrow() {
        buttonArrow?.let { this@ZoomView.removeView(it) }
        buttonArrow = null

    }

    private fun Country.checkVisible(): Boolean {
        val mapboxMapNotNull = mapboxMap ?: return true
        val country = this@checkVisible
        val countryScreenCenter = mapboxMapNotNull.projection.toScreenLocation(country.currentCenter)
        return screenRect.contains(countryScreenCenter.x, countryScreenCenter.y)
    }

    private fun mod360(x: Double): Double {
        return if (x > 0)
            x - Math.floor(x / 360) * 360
        else
            x + Math.ceil(-x / 360) * 360
    }

    private fun rotateTo(newAngle: Double) {
        val view = buttonArrow ?: return
        val offset = Math.floor(currentAngle / 360) * 360
        val newAngle2 = offset + mod360(newAngle)

        val alternativeAngles = doubleArrayOf(newAngle2 - 360, newAngle2, newAngle2 + 360)
        val closestAngle = alternativeAngles.minBy { x -> Math.abs(x - currentAngle) }
        view.rotation = closestAngle!!.toFloat()

        currentAngle = closestAngle
    }

    fun onMapChanged() {
        if (currentCountry?.checkVisible() != false) {
            removeButtonArrow()
            return
        }
        else if (buttonArrow == null) {
            addButtonArrow()
        }
        val country = currentCountry ?: return
        val view = buttonArrow ?: return
        val mapboxMapNotNull = mapboxMap ?: return

        val countryScreenCenter = mapboxMapNotNull.projection.toScreenLocation(country.currentCenter)
        val viewCenter = PointF(
                this@ZoomView.x + view.x + view.width / 2,
                this@ZoomView.y + view.y + view.height / 2
        )
        val direction = Math.atan2(
                (viewCenter.y - countryScreenCenter.y).toDouble(),
                (viewCenter.x - countryScreenCenter.x).toDouble()
        )
        // The arrow points upwards, compensate this
        val rotateAngle = Math.toDegrees(direction) - 90
        rotateTo(rotateAngle)
    }

    fun subscribeOn(observable: Observable<Country>): Disposable {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { country ->
                            currentCountry = country
                        },
                        onComplete = {
                            currentCountry = MercatorApp.shownCountries
                                    .filter { !it.isFixed }
                                    .takeIf { it.isNotEmpty() }
                                    ?.get(0)
                        },
                        onError = { e ->
                            Log.e(TAG, "subscribeOn():", e)
                        }
                )
    }

}