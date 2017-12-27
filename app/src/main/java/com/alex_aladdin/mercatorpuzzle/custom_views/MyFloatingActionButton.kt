package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class MyFloatingActionButton : FloatingActionButton, View.OnClickListener {

    companion object {

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

    private val compositeDisposable = CompositeDisposable()
    private var additionalListener: OnClickListener? = null

    init {
        setOnClickListener(null)
        setColor(defaultColor)
    }

    fun subscribeOn(observable: Observable<Country>) {
        val disposable: Disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { country ->
                    isFocusedOnCountry = false
                    currentCountry = country
                }

        compositeDisposable.add(disposable)
    }

    private fun setColor(color: Int) {
        this@MyFloatingActionButton.backgroundTintList = ColorStateList.valueOf(color)
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
                var index = MercatorApp.shownCountries.indexOf(country)
                if (index >= 0) {
                    index = (index + 1) % MercatorApp.shownCountries.size
                    currentCountry = MercatorApp.shownCountries[index]
                }
                callOnClick()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        compositeDisposable.clear()
    }

}