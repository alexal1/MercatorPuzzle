package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import java.lang.Math.round

class NameView : TextView {

    companion object {

        private const val MASK_SYMBOL = '•'

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val glowRadius = minOf(
            resources.getDimension(R.dimen.name_view_glow_radius),
            MercatorApp.RS_MAX_BLUR_RADIUS
    )

    var countryName: String? = null

    var completeness = 0f   // from 0 to 1
        set(value) {
            field = value

            val nameNotNull = countryName ?: run {
                this@NameView.text = ""
                return
            }
            val length = nameNotNull.length
            val wordLength = round(length.toFloat() * value)
            val builder = StringBuilder()
            (0 until wordLength).forEach { i -> builder.append(nameNotNull[i]) }
            (wordLength until length).forEach { builder.append(MASK_SYMBOL) }
            this@NameView.text = builder.toString()

            glow(wordLength == length)
        }

    var isGlowEnabled = true

    init {
        // Fixes setShadowLayer()'s crash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            this@NameView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this@NameView,
                1,
                resources.getDimension(R.dimen.top_bar_view_font_size).toInt(),
                1,
                TypedValue.COMPLEX_UNIT_PX
        )
    }

    private fun glow(on: Boolean) {
        if (on && isGlowEnabled) {
            this@NameView.setShadowLayer(glowRadius, 0f, 0f, Color.WHITE)
        }
        else {
            this@NameView.setShadowLayer(0f, 0f, 0f, Color.WHITE)
        }
    }

}