package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.google_maps_utils.PolyUtil
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * This class represents one draggable country.
 * Country's vertices are stored as a list of polygons, and each polygon is a list of coordinates.
 */
class Country(var vertices: ArrayList<ArrayList<LatLng>>, val id: String, val name: String) {

    val size = Size()
    val targetCenter = getCenter()
    var currentCenter = targetCenter
        set(value) {
            latitudeBoundaries.check(value)
            updateVertices(value)
            field = value
        }

    private val relativeVertices = RelativeVertices(center = targetCenter, coordinates = vertices)
    private val latitudeBoundaries = LatitudeBoundaries(center = targetCenter, coordinates = vertices)

    /**
     * Update all Country's vertices by moving them according to the new Country's center.
     *
     * @param newCenter coordinates of a new Country's center
     */
    private fun updateVertices(newCenter: LatLng) {
        vertices = relativeVertices.computeAbsoluteCoordinates(newCenter = newCenter)
    }

    /**
     * Calculate country's center.
     */
    private fun getCenter(): LatLng {
        // We find center latitude just by taking half-sum of min and max latitudes
        // To find center longitude we should firstly find boundary longitudes of the country
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
        val centerLat: Double = (minLat + maxLat) / 2
        size.height = Math.abs(maxLat - minLat)

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
        val centerLng: Double
        if (lng1 > lng2) {
            centerLng = (lng1 + lng2) / 2
            size.width = lng1 - lng2
        }
        else {
            centerLng = ((lng1 + lng2) / 2 + 360.0) % 360.0 - 180.0
            size.width = 360.0 - lng2 + lng1
        }

        return LatLng(centerLat, centerLng)
    }

    /**
     * Check if country contains given point or not.
     */
    fun contains(latLng: LatLng): Boolean = vertices.any { polygon -> PolyUtil.containsLocation(latLng, polygon, false) }

    /**
     * Check if Country's currentCenter is close enough to the targetCenter.
     */
    fun isCloseToTarget(): Boolean = Math.abs(targetCenter.longitude - currentCenter.longitude) < size.width / 2
            && Math.abs(targetCenter.latitude - currentCenter.latitude) < size.height / 2

    override fun equals(other: Any?): Boolean = (other is Country) && (other.id == this@Country.id)

    override fun hashCode(): Int = this@Country.id.hashCode()

    /**
     * Country's width and height measured in degrees of longitude and latitude respectively.
     */
    data class Size(var width: Double = 0.0, var height: Double = 0.0)

}