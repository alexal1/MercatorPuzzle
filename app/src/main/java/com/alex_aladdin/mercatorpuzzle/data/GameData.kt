package com.alex_aladdin.mercatorpuzzle.data

data class GameData(val continent: Continents,
                    var coins: Int = 0,
                    val timestampStart: Long,
                    var timestampFinish: Long? = null)