package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

class MyFloatingActionButton : FloatingActionButton, View.OnClickListener {

    companion object {

        const val TAG = "MercatorMyFAB"
        private val defaultColor = ContextCompat.getColor(MercatorApp.applicationContext, R.color.country_fixed)

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

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

    var isFocusedOnCountry = false

    private var additionalListener: OnClickListener? = null

    init {
        setOnClickListener(null)
        setColor(defaultColor)
    }

    fun subscribeOn(observable: Observable<Country>): Disposable {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { country ->
                            isFocusedOnCountry = false
                            currentCountry = country
                        },
                        onComplete = {
                            isFocusedOnCountry = false
                            currentCountry = getNextCountry(null)
                        },
                        onError = { e ->
                            Log.e(TAG, "subscribeOn():", e)
                        }
                )
    }

    private fun setColor(color: Int) {
        this@MyFloatingActionButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun getNextCountry(country: Country?): Country? {
        val availableCountries = MercatorApp.shownCountries.filter { !it.isFixed }
        var index = availableCountries.indexOf(country)
        return when {
            index >= 0 -> {
                index = (index + 1) % availableCountries.size
                availableCountries[index]
            }
            availableCountries.isNotEmpty() -> availableCountries[0]
            else -> null
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        additionalListener = listener
        super.setOnClickListener { view ->
            this@MyFloatingActionButton.onClick(view)
        }
    }

    override fun onClick(view: View?) {
        currentCountry?.let { country ->
            if (!isFocusedOnCountry) {
                isFocusedOnCountry = true
                additionalListener?.onClick(view)
            }
            else {
                isFocusedOnCountry = false
                currentCountry = getNextCountry(country)
                callOnClick()
            }
        }
    }

}