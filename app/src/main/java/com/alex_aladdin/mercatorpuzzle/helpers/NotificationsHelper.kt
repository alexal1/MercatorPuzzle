package com.alex_aladdin.mercatorpuzzle.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.data.Continents
import java.io.Serializable

class NotificationsHelper {

    companion object {

        const val NOTIFICATION_NEW_GAME = "com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper.NOTIFICATION_NEW_GAME"
        const val NOTIFICATION_CONTINENT_CHOSEN = "com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper.NOTIFICATION_CONTINENT_CHOSEN"
        const val NOTIFICATION_COUNTRIES_LOADED = "com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper.NOTIFICATION_COUNTRIES_LOADED"
        const val NOTIFICATION_PROGRESS = "com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper.NOTIFICATION_PROGRESS"

        const val EXTRA_CONTINENT = "com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper.EXTRA_CONTINENT"
        const val EXTRA_PROGRESS = "com.alex_aladdin.mercatorpuzzle.helpers.NotificationsHelper.EXTRA_PROGRESS"

    }

    private val broadcastManager = LocalBroadcastManager.getInstance(MercatorApp.applicationContext)

    /* ------------------------------- Sending notifications -------------------------------------*/

    fun sendNewGameNotification() {
        val intent = Intent(NOTIFICATION_NEW_GAME)
        broadcastManager.sendBroadcast(intent)
    }

    fun sendContinentChosenNotification(continent: Continents) {
        val intent = Intent(NOTIFICATION_CONTINENT_CHOSEN)
        intent.putExtra(EXTRA_CONTINENT, continent as Serializable)
        broadcastManager.sendBroadcast(intent)
    }

    fun sendProgressNotification(progress: Int) {
        val intent = Intent(NOTIFICATION_PROGRESS)
        intent.putExtra(EXTRA_PROGRESS, progress)
        broadcastManager.sendBroadcast(intent)
    }

    fun sendCountriesLoadedNotification() {
        val intent = Intent(NOTIFICATION_COUNTRIES_LOADED)
        broadcastManager.sendBroadcast(intent)
    }

    /* ------------------------------ Receiving notifications ------------------------------------*/

    fun registerNewGameReceiver(listener: () -> Unit): BroadcastReceiver {
        val newGameReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                listener()
            }

        }
        broadcastManager.registerReceiver(newGameReceiver, IntentFilter(NOTIFICATION_NEW_GAME))
        return newGameReceiver
    }

    fun registerContinentChosenReceiver(listener: (continent: Continents) -> Unit): BroadcastReceiver {
        val continentChosenReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                val continent = intent?.getSerializableExtra(EXTRA_CONTINENT) as? Continents ?: return
                listener(continent)
            }

        }
        broadcastManager.registerReceiver(continentChosenReceiver, IntentFilter(NOTIFICATION_CONTINENT_CHOSEN))
        return continentChosenReceiver
    }

    fun registerProgressReceiver(listener: (progress: Int) -> Unit): BroadcastReceiver {
        val progressReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                val progress = intent?.getSerializableExtra(EXTRA_PROGRESS) as? Int ?: return
                listener(progress)
            }

        }
        broadcastManager.registerReceiver(progressReceiver, IntentFilter(NOTIFICATION_PROGRESS))
        return progressReceiver
    }

    fun registerCountriesLoadedReceiver(listener: () -> Unit): BroadcastReceiver {
        val countriesLoadedReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                listener()
            }

        }
        broadcastManager.registerReceiver(countriesLoadedReceiver, IntentFilter(NOTIFICATION_COUNTRIES_LOADED))
        return countriesLoadedReceiver
    }

    fun unregisterReceiver(receiver: BroadcastReceiver) {
        broadcastManager.unregisterReceiver(receiver)
    }

}