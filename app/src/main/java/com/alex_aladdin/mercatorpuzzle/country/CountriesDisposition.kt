package com.alex_aladdin.mercatorpuzzle.country

import android.util.Log
import com.alex_aladdin.mercatorpuzzle.activities.MapActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

/**
 * Class that performs initial disposition of given countries on a certain area of the map.
 *
 * @param viewPort area on the map, on which all countries will be placed
 */
class CountriesDisposition(private val viewPort: MapActivity.ViewPort = MapActivity.ViewPort()) {

    companion object {

        const val TAG = "MercatorCDisposition"

    }

    private val random = Random()

    fun apply(countries: List<Country>) {
        for (country in countries) {
            // Set random latitude within viewPort
            val latitudeBoundaries = LatitudeBoundaries(
                    center = country.currentCenter,
                    coordinates = country.vertices,
                    maxLatitude = viewPort.northeast.latitude,
                    minLatitude = viewPort.southwest.latitude
            )
            val centersAreaBottom = latitudeBoundaries.centerMin
            val centersAreaTop = latitudeBoundaries.centerMax
            val newLatitude = if (centersAreaBottom < centersAreaTop) {
                getRandomInRange(centersAreaBottom, centersAreaTop)
            }
            else {
                Log.e(TAG, "Not enough space vertically for ${country.name}:\n"
                        + "centersAreaBottom = $centersAreaBottom, centersAreaTop = $centersAreaTop")
                (centersAreaBottom + centersAreaTop) / 2
            }
            country.currentCenter = LatLng(newLatitude, 0.0)

            // Find difference between country center (0.0) and it's rect center
            val rect = country.getRect()
            val diff = rect.leftLng + rect.width / 2

            // Find length of viewport
            val west = viewPort.southwest.longitude
            val east = viewPort.northeast.longitude
            val length = if (west < east) {
                east - west
            }
            else {
                360.0 - west + east
            }

            // Find random offset for rect center.
            // It will be added to the west of the viewport plus country's half width.
            val offset = if (length > rect.width) {
                getRandomInRange(0.0, length - rect.width)
            }
            else {
                Log.e(TAG, "Not enough space horizontally for ${country.name}:\n"
                        + "length = $length, width = ${rect.width}")
                (length - rect.width) / 2
            }

            // Find new longitude within viewport
            var newLongitude = west + rect.width / 2 + offset - diff
            if (newLongitude > 180.0) {
                newLongitude = -180.0 + newLongitude % 180.0
            }
            else if (newLongitude < -180.0) {
                newLongitude = 180.0 - newLongitude % 180.0
            }

            country.currentCenter = LatLng(country.currentCenter.latitude, newLongitude)
        }
    }

    private fun getRandomInRange(a: Double, b: Double): Double = a + (b - a) * random.nextDouble()

}