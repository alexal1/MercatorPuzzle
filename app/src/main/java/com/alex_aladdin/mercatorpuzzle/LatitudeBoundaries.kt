package com.alex_aladdin.mercatorpuzzle

import android.util.Log
import com.alex_aladdin.utils.CardinalDirection
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Class that calculates allowable range of latitudes for Country's center.
 * It is also used to check whether point lies in this range.
 */
class LatitudeBoundaries(private val center: LatLng, private val coordinates: ArrayList<ArrayList<LatLng>>) {

    val LOG_TAG = "MercatorLBoundaries"
    private var centerMax = MAX_LATITUDE
    private var centerMin = -MAX_LATITUDE

    companion object {

        val MAX_LATITUDE = 85.06

    }

    init {
        // NORTH
        val pointNorth = findPointClosestToPole(CardinalDirection.NORTH)
        val relativeToNorth = RelativeVertices(center = pointNorth, coordinates = arrayListOf(arrayListOf(center)))
        centerMax = relativeToNorth
                .computeAbsoluteCoordinates(newCenter = LatLng(MAX_LATITUDE, pointNorth.longitude))[0][0]
                .latitude

        // SOUTH
        val pointSouth = findPointClosestToPole(CardinalDirection.SOUTH)
        val relativeToSouth = RelativeVertices(center = pointSouth, coordinates = arrayListOf(arrayListOf(center)))
        centerMin = relativeToSouth
                .computeAbsoluteCoordinates(newCenter = LatLng(-MAX_LATITUDE, pointSouth.longitude))[0][0]
                .latitude

        Log.i(LOG_TAG, "Min center latitude: $centerMin, max center latitude: $centerMax")
    }

    /**
     * Find point that is to the NORTH (to the SOUTH) of the center and at the same time
     * is closest to it by longitude.
     *
     * @param direction must be either NORTH or SOUTH
     */
    private fun findPointClosestToPole(direction: CardinalDirection): LatLng {
        return coordinates
                .flatten()
                .filter { latLng ->
                    if (direction == CardinalDirection.NORTH)
                        latLng.latitude > center.latitude
                    else
                        latLng.latitude < center.latitude
                }
                .minBy { latLng ->
                    Math.abs(latLng.longitude - center.longitude)
                }!!
    }

    /**
     * Check whether new center's latitude lies in allowable range and correct it if necessary.
     */
    fun check(newCenter: LatLng) {
        if (newCenter.latitude > centerMax)
            newCenter.latitude = centerMax
        else if (newCenter.latitude < centerMin)
            newCenter.latitude = centerMin
    }

}