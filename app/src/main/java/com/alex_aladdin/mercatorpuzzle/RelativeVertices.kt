package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.utils.position
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.services.api.utils.turf.TurfConstants
import com.mapbox.services.api.utils.turf.TurfMeasurement

/**
 * One Country's vertices expressed through bearing + distance relatively to the center point.
 *
 * @param center        coordinates of a center point
 * @param coordinates   coordinates of all vertices in absolute values
 */
class RelativeVertices(center: LatLng, coordinates: ArrayList<ArrayList<LatLng>>) {

    private val vertices: ArrayList<ArrayList<Vertex>> = initVertices(center, coordinates)

    /**
     * Compute relative vertices by absolute coordinates.
     */
    private fun initVertices(center: LatLng, coordinates: ArrayList<ArrayList<LatLng>>): ArrayList<ArrayList<Vertex>> {
        val result: ArrayList<ArrayList<Vertex>> = ArrayList()

        val centerPos = center.position()
        coordinates.forEachIndexed { i, polygon ->
            result.add(i, ArrayList<Vertex>())
            polygon.forEachIndexed { j, latLng ->
                val vertex = Vertex(
                        bearing = TurfMeasurement.bearing(centerPos, latLng.position()),
                        distance = TurfMeasurement.distance(centerPos, latLng.position(), TurfConstants.UNIT_DEFAULT)
                )
                result[i].add(j, vertex)
            }
        }

        return result
    }

    /**
     * Compute vertices' new absolute coordinates.
     *
     * @param newCenter absolute coordinates will be obtained relatively to this point
     * @return new absolute coordinates of Country's vertices
     */
    fun computeAbsoluteCoordinates(newCenter: LatLng): ArrayList<ArrayList<LatLng>> {
        val result: ArrayList<ArrayList<LatLng>> = ArrayList()

        val centerPos = newCenter.position()
        vertices.forEachIndexed { i, polygon ->
            result.add(i, ArrayList<LatLng>())
            polygon.forEachIndexed { j, vertex ->
                val pos = TurfMeasurement.destination(centerPos, vertex.distance, vertex.bearing, TurfConstants.UNIT_DEFAULT)
                result[i].add(j, LatLng(pos.latitude, pos.longitude))
            }
        }

        return result
    }

    /**
     * Relative vertex is determined by bearing and distance.
     */
    private class Vertex(val bearing: Double, val distance: Double)

}