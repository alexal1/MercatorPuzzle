package com.alex_aladdin.mercatorpuzzle

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.content.res.Configuration
import android.graphics.PointF
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.alex_aladdin.mercatorpuzzle.data.AppDatabase
import com.alex_aladdin.mercatorpuzzle.data.GameData
import com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Extending Application class.
 */
class MercatorApp : Application() {

    companion object {

        private const val TAG = "MercatorApp"
        private const val DATABASE_NAME = "mercator_database"
        const val SHARED_PREFERENCES_NAME = "mercator_shared_preferences"
        const val SHARED_PREFERENCES_FEEDBACK_GIVEN = "is_feedback_given"
        const val RS_MAX_BLUR_RADIUS = 25f

        lateinit var applicationContext: Context private set
        lateinit var screen: PointF private set
        lateinit var gameController: GameController
        lateinit var notificationsHelper: NotificationsHelper
        lateinit var appDatabase: AppDatabase

        var flagDoNotShowFeedbackDialog = false
        var gameData: GameData? = null
        val loadedCountries = ArrayList<Country>()
        val shownCountries = ArrayList<Country>()
        val countryFixedColor by lazy { ContextCompat.getColor(MercatorApp.applicationContext, R.color.country_fixed) }

        private lateinit var countryColors: IntArray
        private val random = Random()

        /**
         * Returns random color from countryColors that's not used in any of shownCountries yet.
         */
        fun obtainColor(): Int {
            fun getRandomFromList(list: List<Int>): Int {
                val i = random.nextInt(list.size)
                return list[i]
            }

            val range = countryColors.size - shownCountries.size
            return if (range > 0) {
                val allowableColors = ArrayList<Int>(range)
                countryColors.forEach { color ->
                    if (shownCountries.none { it.color == color }) {
                        allowableColors.add(color)
                    }
                }
                getRandomFromList(allowableColors)
            }
            else {
                Log.e(TAG, "No free colors: number of shown countries is larger than number of colors")
                getRandomFromList(countryColors.toList())
            }
        }

    }

    override fun onCreate() {
        super.onCreate()

        Companion.applicationContext = this.applicationContext
        Companion.gameController = GameController()
        Companion.notificationsHelper = NotificationsHelper()
        Companion.appDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DATABASE_NAME).build()
        Companion.countryColors = resources.getIntArray(R.array.country_colors)

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