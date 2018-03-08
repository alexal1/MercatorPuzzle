package com.alex_aladdin.mercatorpuzzle.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface GameDataDao {

    @Insert
    fun insert(gameData: GameData): Long

    @Query("SELECT * FROM GameData ORDER BY timestampStart DESC")
    fun getAll(): List<GameData>

}