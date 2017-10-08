package com.alex_aladdin.mercatorpuzzle

import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

/**
 * Class that performs initial disposition of given countries on a certain area of the map.
 *
 * @param viewPort area on the map, on which all countries will be placed
 * @param difficulty dispersion around the target position, measured in country's sizes
 */
class CountriesDisposition(private val viewPort: MapActivity.ViewPort = MapActivity.ViewPort(),
                           private val difficulty: Int = 5) {

    private val random = Random()

    fun apply(countries: List<Country>) {
        for (country in countries) {
            // Limitations #1: by ViewPort
            val longitudeStart1 = viewPort.southwest.longitude + country.size.width / 2
            val longitudeEnd1 = viewPort.northeast.longitude - country.size.width / 2
            val latitudeStart1 = viewPort.southwest.latitude + country.size.height / 2
            val latitudeEnd1 = viewPort.northeast.latitude - country.size.height / 2

            if (longitudeStart1 > longitudeEnd1 || latitudeStart1 > latitudeEnd1) {
                throw IllegalArgumentException("Wrong or too small ViewPort!")
            }

            // Limitations #2: by difficulty
            val longitudeStart2 = country.targetCenter.longitude - country.size.width * difficulty
            val longitudeEnd2 = country.targetCenter.longitude + country.size.width * difficulty
            val latitudeStart2 = country.targetCenter.latitude - country.size.height * difficulty
            val latitudeEnd2 = country.targetCenter.latitude + country.size.height * difficulty

            // Obtain final limitations
            val longitudeStart = if (longitudeStart2 < longitudeEnd1)
                maxOf(longitudeStart1, longitudeStart2)
            else
                longitudeStart1
            val longitudeEnd = if (longitudeEnd2 > longitudeStart1)
                minOf(longitudeEnd1, longitudeEnd2)
            else
                longitudeEnd1
            val latitudeStart = if (latitudeStart2 < latitudeEnd1)
                maxOf(latitudeStart1, latitudeStart2)
            else
                latitudeStart1
            val latitudeEnd = if (latitudeEnd2 > latitudeStart1)
                minOf(latitudeEnd1, latitudeEnd2)
            else
                latitudeEnd1

            // Obtain random value
            country.currentCenter = LatLng(
                    getRandomInRange(latitudeStart, latitudeEnd),
                    getRandomInRange(longitudeStart, longitudeEnd)
            )
        }
    }

    private fun getRandomInRange(a: Double, b: Double): Double = a + (b - a) * random.nextDouble()

}