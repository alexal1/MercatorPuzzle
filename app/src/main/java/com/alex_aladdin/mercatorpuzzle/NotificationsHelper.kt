package com.alex_aladdin.mercatorpuzzle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.alex_aladdin.mercatorpuzzle.data.Continents
import java.io.Serializable

class NotificationsHelper {

    companion object {

        const val NOTIFICATION_NEW_GAME = "com.alex_aladdin.mercatorpuzzle.NotificationsHelper.NOTIFICATION_NEW_GAME"
        const val NOTIFICATION_CONTINENT_CHOSEN = "com.alex_aladdin.mercatorpuzzle.NotificationsHelper.NOTIFICATION_CONTINENT_CHOSEN"

        const val EXTRA_CONTINENT = "com.alex_aladdin.mercatorpuzzle.NotificationsHelper.EXTRA_CONTINENT"

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

    fun unregisterReceiver(receiver: BroadcastReceiver) {
        broadcastManager.unregisterReceiver(receiver)
    }

}