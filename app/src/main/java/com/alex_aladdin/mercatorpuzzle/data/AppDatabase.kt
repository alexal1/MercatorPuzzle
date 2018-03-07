package com.alex_aladdin.mercatorpuzzle.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.alex_aladdin.mercatorpuzzle.helpers.ContinentTypeConverter

@Database(entities = [GameData::class], version = 1, exportSchema = false)
@TypeConverters(ContinentTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDataDao() : GameDataDao

}