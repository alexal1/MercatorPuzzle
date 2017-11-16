package com.alex_aladdin.mercatorpuzzle.country

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Rectangle in longitude/latitude degrees that circumscribes Country.
 */
data class CountryRect(val leftLng: Double, val topLat: Double, val rightLng: Double, val bottomLat: Double) {

    val center: LatLng by lazy {
        val centerLat: Double = (topLat + bottomLat) / 2
        val centerLng: Double = if (leftLng < rightLng) {
            (leftLng + rightLng) / 2
        }
        else {
            ((leftLng + rightLng) / 2 + 360.0) % 360.0 - 180.0
        }
        LatLng(centerLat, centerLng)
    }

    val height: Double by lazy {
        topLat - bottomLat
    }

    val width: Double by lazy {
        if (leftLng < rightLng) {
            rightLng - leftLng
        }
        else {
            360.0 - leftLng + rightLng
        }
    }

}