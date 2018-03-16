package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R

class CoinsCounterView : TextView {

    companion object {

        private const val DELAY = 10L

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val colorWhite = ContextCompat.getColor(context, R.color.white)
    private val colorGold = ContextCompat.getColor(context, R.color.gold)
    private val glowRadius = minOf(
            resources.getDimension(R.dimen.coins_counter_view_glow_radius),
            MercatorApp.RS_MAX_BLUR_RADIUS
    )

    init {
        // Fixes setShadowLayer()'s crash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            this@CoinsCounterView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private var currentValue = 0
        set(value) {
            field = value
            val text = "+$value"
            this@CoinsCounterView.post {
                this@CoinsCounterView.text = text
            }
        }

    private fun glow(on: Boolean) {
        this@CoinsCounterView.post {
            if (on) {
                this@CoinsCounterView.setTextColor(colorGold)
                this@CoinsCounterView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_stack_gold, 0)
                this@CoinsCounterView.setShadowLayer(glowRadius, 0f, 0f, colorGold)
            }
            else {
                this@CoinsCounterView.setTextColor(colorWhite)
                this@CoinsCounterView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_stack_white, 0)
                this@CoinsCounterView.setShadowLayer(0f, 0f, 0f, colorWhite)
            }
        }
    }

    fun showIncome(amount: Int) {
        currentValue = 0
        glow(false)
        Thread({
            while (currentValue < amount) {
                currentValue++
                Thread.sleep(DELAY)
            }
            glow(true)
        }, "CoinsCounterViewThread").start()
    }

}