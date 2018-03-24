package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.zoom_view.view.*

class ZoomView : LinearLayout {

    companion object {

        const val TAG = "MercatorZoomView"

    }

    enum class Zoom { IN, OUT }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.zoom_view_button_background)
    private val defaultColor = ContextCompat.getColor(context, R.color.country_fixed)
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
        }
    var onButtonClick: (zoom: Zoom) -> Unit = {}

    init {
        inflate(context, R.layout.zoom_view, this@ZoomView)
        this@ZoomView.orientation = LinearLayout.VERTICAL
        this@ZoomView.setBackgroundResource(R.drawable.zoom_view_background)
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