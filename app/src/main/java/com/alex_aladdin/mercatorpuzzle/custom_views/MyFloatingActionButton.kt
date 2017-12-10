package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R

class MyFloatingActionButton : FloatingActionButton {

    companion object {

        private val defaultColor = ContextCompat.getColor(MercatorApp.applicationContext, R.color.country_fixed)

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setColor(defaultColor)
    }

    private fun setColor(color: Int) {
        this@MyFloatingActionButton.backgroundTintList = ColorStateList.valueOf(color)
    }

}