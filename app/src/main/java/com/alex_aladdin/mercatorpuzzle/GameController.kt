package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.mercatorpuzzle.data.Continents

class GameController {

    fun newGame() {
        MercatorApp.notificationsHelper.sendNewGameNotification()
    }

    fun chooseContinent(continent: Continents) {
        MercatorApp.notificationsHelper.sendContinentChosenNotification(continent)
    }

}