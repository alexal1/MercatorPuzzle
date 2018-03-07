package com.alex_aladdin.mercatorpuzzle

import android.util.Log
import com.alex_aladdin.mercatorpuzzle.data.Continents
import com.alex_aladdin.mercatorpuzzle.data.GameData
import com.alex_aladdin.mercatorpuzzle.data.GeoJsonParser
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.math.ceil

class GameController {

    companion object {

        const val TAG = "MercatorGameController"
        const val LAP_PORTION = 5

    }

    private var currentLapCoinsAmount = 0

    fun newGame() {
        MercatorApp.apply {
            gameData = null
            shownCountries.clear()
            loadedCountries.clear()
            notificationsHelper.sendNewGameNotification()
        }
    }

    fun chooseContinent(continent: Continents) {
        MercatorApp.apply {
            gameData = GameData(continent = continent, timestampStart = System.currentTimeMillis())
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
        currentLapCoinsAmount = 0
        MercatorApp.apply {
            shownCountries.clear()
            val unfixedCountries = loadedCountries.filter { !it.isFixed }.shuffled()
            if (unfixedCountries.isNotEmpty()) {
                notificationsHelper.sendNewLapNotification(unfixedCountries.take(LAP_PORTION))
            }
            else {
                gameData?.timestampFinish = System.currentTimeMillis()
                saveGame()
                notificationsHelper.sendFinishGameNotification()
            }
        }
    }

    fun saveGame() {
        MercatorApp.apply {
            val gd = gameData ?: return
            Observable
                    .fromCallable {
                        appDatabase.gameDataDao().insert(gd)
                    }
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.i(TAG, "Game saved at row $it")
                    }
        }
    }

    fun loadAllGames(completion: (List<GameData>) -> Unit) {
        MercatorApp.apply {
            Observable
                    .fromCallable {
                        appDatabase.gameDataDao().getAll()
                    }
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.i(TAG, "Loaded ${it.size} games")
                        completion(it)
                    }
        }
    }

    fun addIncome(amount: Int) {
        currentLapCoinsAmount += amount
        MercatorApp.gameData!!.coins += amount
    }

    fun getLapIncome(): Int {
        return currentLapCoinsAmount
    }

}