package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.mercatorpuzzle.data.Continents
import com.alex_aladdin.mercatorpuzzle.data.GeoJsonParser
import kotlin.math.ceil

class GameController {

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

}