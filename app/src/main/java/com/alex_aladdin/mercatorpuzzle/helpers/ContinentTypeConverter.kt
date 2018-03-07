package com.alex_aladdin.mercatorpuzzle.helpers

import android.arch.persistence.room.TypeConverter
import com.alex_aladdin.mercatorpuzzle.data.Continents

class ContinentTypeConverter {

    @TypeConverter
    fun toContinent(string: String): Continents {
        return Continents.valueOf(string)
    }

    @TypeConverter
    fun toString(continent: Continents): String {
        return continent.name
    }

}