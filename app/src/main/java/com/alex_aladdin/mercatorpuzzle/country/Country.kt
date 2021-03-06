package com.alex_aladdin.mercatorpuzzle.country

import android.graphics.Color
import com.alex_aladdin.google_maps_utils.PolyUtil
import com.alex_aladdin.google_maps_utils.SphericalUtil
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * This class represents one draggable country.
 * Country's vertices are stored as a list of polygons, and each polygon is a list of coordinates.
 */
class Country(var vertices: ArrayList<ArrayList<LatLng>>, val id: String, val name: String) {

    private val initRect = getRect()

    val targetCenter = initRect.center
    var currentCenter = targetCenter
        set(value) {
            latitudeBoundaries.check(value)
            updateVertices(value)
            updateDistanceToTarget(value)
            field = value
            currentCenterSubject.onNext(this@Country)
        }
    var color: Int = Color.TRANSPARENT
    var distanceToTarget = 0.0
    var isFixed = false
        set(value) {
            field = value
            color = MercatorApp.countryFixedColor
            currentCenterSubject.onComplete()
        }
    val area = vertices.sumByDouble { polygon -> SphericalUtil.computeArea(polygon) }

    private val relativeVertices = RelativeVertices(center = targetCenter, coordinates = vertices)
    private val latitudeBoundaries = LatitudeBoundaries(center = targetCenter, coordinates = vertices)
    private val currentCenterSubject: PublishSubject<Country> = PublishSubject.create<Country>()
    val currentCenterObservable: Observable<Country> = currentCenterSubject

    /**
     * Update all Country's vertices by moving them according to the new Country's center.
     *
     * @param newCenter coordinates of a new Country's center
     */
    private fun updateVertices(newCenter: LatLng) {
        vertices = relativeVertices.computeAbsoluteCoordinates(newCenter = newCenter)
    }

    /**
     * Update rhumb distance from the current center to the target center.
     */
    private fun updateDistanceToTarget(newCenter: LatLng) {
        distanceToTarget = SphericalUtil.rhumbDistance(newCenter, targetCenter)
    }

    fun getRect(): CountryRect {
        // Top and bottom are found simply as max and min latitudes.
        // To find left and right we firstly find largest distance between longitudes
        // and then take its boundaries.
        val longitudes: ArrayList<Double> = ArrayList()

        var minLat: Double = vertices[0][0].latitude
        var maxLat: Double = vertices[0][0].latitude
        for (polygon in vertices) {
            for (latLng in polygon) {
                longitudes.add(latLng.longitude)
                if (latLng.latitude < minLat) minLat = latLng.latitude
                if (latLng.latitude > maxLat) maxLat = latLng.latitude
            }
        }

        longitudes.sort()

        val n = longitudes.size - 1
        var maxDistance = 0.0
        var lng1 = 0.0
        var lng2 = 0.0
        (0..n).forEach { i ->
            val distance = if (i < n)
                longitudes[i+1] - longitudes[i]
            else
                longitudes[0] + 360.0 - longitudes[n]

            // If this is longest distance, save its boundaries
            if (distance > maxDistance) {
                maxDistance = distance
                if (i < n) {
                    lng1 = longitudes[i]
                    lng2 = longitudes[i+1]
                }
                else {
                    lng1 = longitudes[n]
                    lng2 = longitudes[0]
                }
            }
        }

        // Revert lng1 and lng2 as we need boundaries of the country, not of the longest distance
        return CountryRect(leftLng = lng2, topLat = maxLat, rightLng = lng1, bottomLat = minLat)
    }

    /**
     * Check if country contains given point or not.
     */
    fun contains(latLng: LatLng): Boolean = vertices.any { polygon -> PolyUtil.containsLocation(latLng, polygon, false) }

    /**
     * Check if this and given countries are both covering some Earth's area.
     * It is a quite time-consuming function that should be used rarely.
     */
    fun intersects(c: Country): Boolean = vertices.flatten().any { vertex -> c.contains(vertex) }
            || c.vertices.flatten().any { vertex -> contains(vertex) }

    /**
     * Check if Country's currentCenter is close enough to the targetCenter.
     */
    fun isCloseToTarget(): Boolean {
        val distY = Math.abs(targetCenter.latitude - currentCenter.latitude)
        val distX = minOf(
                Math.abs(targetCenter.longitude - currentCenter.longitude),
                360.0 - Math.abs(targetCenter.longitude - currentCenter.longitude)
        )
        return (distX < initRect.width / 2 && distY < initRect.height / 2)
    }

    override fun equals(other: Any?): Boolean = (other is Country) && (other.id == this@Country.id)

    override fun hashCode(): Int = this@Country.id.hashCode()

}