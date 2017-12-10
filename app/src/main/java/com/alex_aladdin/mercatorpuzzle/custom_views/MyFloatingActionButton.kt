package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.alex_aladdin.mercatorpuzzle.MapActivity
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class MyFloatingActionButton : FloatingActionButton, View.OnClickListener, PropertyChangeListener {

    companion object {

        private val defaultColor = ContextCompat.getColor(MercatorApp.applicationContext, R.color.country_fixed)

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var currentCountry: Country? = null
        set(value) {
            field = value

            if (value != null) {
                setColor(value.color)
            }
            else {
                setColor(defaultColor)
            }
        }

    var isFocusedOnCountry = false

    init {
        setOnClickListener(this@MyFloatingActionButton)
        setColor(defaultColor)
    }

    private fun setColor(color: Int) {
        this@MyFloatingActionButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun onClick(view: View?) {
        currentCountry?.let { country ->
            if (!isFocusedOnCountry) {
                isFocusedOnCountry = true
                (context as? MapActivity)?.focusCameraOn(country)
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

    override fun propertyChange(pce: PropertyChangeEvent?) {
        pce ?: return

        if (pce.propertyName == Country.PROPERTY_CURRENT_CENTER) {
            isFocusedOnCountry = false
            currentCountry = pce.source as Country
        }
    }

}