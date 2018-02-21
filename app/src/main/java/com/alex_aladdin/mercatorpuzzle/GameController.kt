package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.mercatorpuzzle.data.Continents
import com.alex_aladdin.mercatorpuzzle.data.GeoJsonParser
import kotlin.math.ceil

class GameController {

    companion object {

        const val LAP_PORTION = 5

    }

    fun newGame() {
        MercatorApp.apply {
            currentContinent = null
            shownCountries.clear()
            loadedCountries.clear()
            notificationsHelper.sendNewGameNotification()
        }
    }

    fun chooseContinent(continent: Continents) {
        MercatorApp.apply {
            currentContinent = continent
            notificationsHelper.sendContinentChosenNotification(continent)
            GeoJsonParser(
                    completion = { countries ->
                        loadedCountries.addAll(countries)
                        notificationsHelper.sendCountriesLoadedNotification()
                    },
                    progress = { current ->
                        val currentInt = ceil(current * 100).toInt()
                        notificationsHelper.sendProgressNotification(currentInt)
                    }
            ).execute(continent)
        }
    }

    fun readyForNextLap() {
        MercatorApp.apply {
            shownCountries.clear()
            val unfixedCountries = loadedCountries.filter { !it.isFixed }.shuffled()
            if (unfixedCountries.isNotEmpty()) {
                notificationsHelper.sendNewLapNotification(unfixedCountries.take(LAP_PORTION))
            }
            else {
                notificationsHelper.sendFinishGameNotification()
            }
        }
    }

}