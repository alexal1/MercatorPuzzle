package com.alex_aladdin.mercatorpuzzle

import android.app.Application
import android.content.Context

/**
 * Extending Application class.
 */
class MercatorApp : Application() {

    companion object {

        lateinit var applicationContext: Context private set

    }

    override fun onCreate() {
        super.onCreate()

        Companion.applicationContext = this.applicationContext
    }

}