package com.alex_aladdin.mercatorpuzzle

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.PointF
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Extending Application class.
 */
class MercatorApp : Application() {

    companion object {

        lateinit var applicationContext: Context private set
        lateinit var screen: PointF private set

        val loadedCountries = ArrayList<Country>()

    }

    override fun onCreate() {
        super.onCreate()

        Companion.applicationContext = this.applicationContext

        getScreenSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        getScreenSize()
    }

    /**
     * Save screen sizes to Companion object.
     */
    private fun getScreenSize() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        Companion.screen = PointF(metrics.widthPixels.toFloat(), metrics.heightPixels.toFloat())
    }

}