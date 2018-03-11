package com.alex_aladdin.mercatorpuzzle.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity
data class GameData(@PrimaryKey(autoGenerate = true) val id: Int,
                    var continent: Continents?,
                    var progress: Int,
                    var coins: Int,
                    var timestampStart: Long?,
                    var timestampFinish: Long?) {

    @Ignore
    constructor() : this(
            id = 0,
            continent = null,
            progress = 0,
            coins = 0,
            timestampStart = null,
            timestampFinish = null
    )

    fun isStarted(): Boolean = timestampStart != null

    fun isFinished(): Boolean = timestampFinish != null

}