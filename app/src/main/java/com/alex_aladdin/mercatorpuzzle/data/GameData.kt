package com.alex_aladdin.mercatorpuzzle.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class GameData(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                    val continent: Continents,
                    var progress: Int? = null,
                    var coins: Int = 0,
                    val timestampStart: Long,
                    var timestampFinish: Long? = null)