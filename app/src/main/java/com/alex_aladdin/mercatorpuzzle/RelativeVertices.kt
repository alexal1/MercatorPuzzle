package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.utils.google_maps_utils.SphericalUtil
import com.mapbox.mapboxsdk.geometry.LatLng

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

        coordinates.forEachIndexed { i, polygon ->
            result.add(i, ArrayList<Vertex>())
            polygon.forEachIndexed { j, latLng ->
                val vertex = Vertex(
                        heading = SphericalUtil.computeHeading(center, latLng),
                        distance = SphericalUtil.computeDistanceBetween(center, latLng)
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

        vertices.forEachIndexed { i, polygon ->
            result.add(i, ArrayList<LatLng>())
            polygon.forEachIndexed { j, vertex ->
                val latLng = SphericalUtil.computeOffset(newCenter, vertex.distance, vertex.heading)
                result[i].add(j, latLng)
            }
        }

        return result
    }

    /**
     * Relative vertex is determined by bearing and distance.
     */
    private class Vertex(val heading: Double, val distance: Double)

}