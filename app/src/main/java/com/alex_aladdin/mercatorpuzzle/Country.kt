package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.google_maps_utils.PolyUtil
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * This class represents one draggable country.
 * Country's vertices are stored as a list of polygons, and each polygon is a list of coordinates.
 */
class Country(var vertices: ArrayList<ArrayList<LatLng>>, val id: String, val name: String) {

    val targetCenter = getCenter()
    private val relativeVertices = RelativeVertices(center = targetCenter, coordinates = vertices)
    private val latitudeBoundaries = LatitudeBoundaries(center = targetCenter, coordinates = vertices)

    /**
     * Update all Country's vertices by moving them according to the new Country's center.
     *
     * @param newCenter coordinates of a new Country's center
     */
    fun updateVertices(newCenter: LatLng) {
        latitudeBoundaries.check(newCenter)
        vertices = relativeVertices.computeAbsoluteCoordinates(newCenter = newCenter)
    }

    /**
     * Calculate country's center.
     */
    fun getCenter(): LatLng {
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
        val centerLng = if (lng1 > lng2)
            (lng1 + lng2) / 2
        else
            ((lng1 + lng2) / 2 + 360.0) % 360.0 - 180.0

        return LatLng(centerLat, centerLng)
    }

    /**
     * Check if country contains given point or not.
     */
    fun contains(latLng: LatLng): Boolean {
        return vertices.any { polygon -> PolyUtil.containsLocation(latLng, polygon, false) }
    }

}