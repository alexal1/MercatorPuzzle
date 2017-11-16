package com.alex_aladdin.mercatorpuzzle.country

import com.alex_aladdin.google_maps_utils.MathUtil
import com.alex_aladdin.google_maps_utils.SphericalUtil
import com.mapbox.mapboxsdk.geometry.LatLng
import java.lang.Math.*

/**
 * Class that calculates allowable range of latitudes for Country's center.
 * It is also used to check whether point lies in this range.
 */
class LatitudeBoundaries(val center: LatLng,
                         val coordinates: List<List<LatLng>>,
                         maxLatitude: Double = +MAX_MAP_LATITUDE,
                         minLatitude: Double = -MAX_MAP_LATITUDE) {

    companion object {

        const val MAX_MAP_LATITUDE = 85.06
        private val R = MathUtil.EARTH_RADIUS

    }

    private enum class Direction { NORTH, SOUTH }

    var centerMax = maxLatitude
        private set
    var centerMin = minLatitude
        private set

    init {
        val distanceToNorth: Double
        val distanceToSouth: Double

        // Distances to the edges of the world
        val distanceToNorthEdge = getDistanceToPole(Direction.NORTH, +MAX_MAP_LATITUDE)
        val distanceToSouthEdge = getDistanceToPole(Direction.SOUTH, -MAX_MAP_LATITUDE)

        distanceToNorth = if (maxLatitude == MAX_MAP_LATITUDE) {
            distanceToNorthEdge
        }
        else {
            max(-distanceToSouthEdge, getDistanceToPole(Direction.NORTH, maxLatitude))
        }

        distanceToSouth = if (minLatitude == -MAX_MAP_LATITUDE) {
            distanceToSouthEdge
        }
        else {
            max(-distanceToNorthEdge, getDistanceToPole(Direction.SOUTH, minLatitude))
        }

        computeCenterBoundaries(distanceToNorth, distanceToSouth)
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

    /**
     * Find normal vector for the plane that includes Country center's meridian.
     */
    private fun findNormalVector(center: LatLng): CartesianVector {
        val c = center.toCartesian()
        return CartesianVector(c.y * R, -c.x * R, 0.0).normalize()
    }

    private fun computeCenterBoundaries(distanceToNorth: Double, distanceToSouth: Double) {
        centerMax = SphericalUtil.computeOffset(center, distanceToNorth, 0.0).latitude
        centerMin = SphericalUtil.computeOffset(center, distanceToSouth, 180.0).latitude
    }

    /**
     * Get distance to the pole with given latitude in given direction.
     *
     * If direction to the pole coincides with given direction, then distance is positive. It is a
     * max distance by which country's center can be brought closer to the pole.
     *
     * If direction to the pole doesn't coincide with given direction, then distance is negative. It
     * is a min distance by which country's center must be brought closer to the pole.
     */
    private fun getDistanceToPole(direction: Direction, latitude: Double): Double {
        val n = findNormalVector(center)
        val z0 = R * sin(latitude.toRadians())
        val allPoints: List<LatLng> = coordinates.flatten()
        val (abovePolePoints, belowPolePoints) = allPoints.partition { point -> point.latitude > latitude }
        val distance: Double

        when (direction) {
            Direction.NORTH -> {
                distance = if (abovePolePoints.isEmpty()) {
                    belowPolePoints.map { point -> getDistanceToPole(n, z0, point) ?: Double.MAX_VALUE }.min()!!
                }
                else {
                    -abovePolePoints.map { point -> getDistanceToPole(n, z0, point) ?: 0.0 }.max()!!
                }
            }
            Direction.SOUTH -> {
                distance = if (belowPolePoints.isEmpty()) {
                    abovePolePoints.map { point -> getDistanceToPole(n, z0, point) ?: Double.MAX_VALUE }.min()!!
                }
                else {
                    -belowPolePoints.map { point -> getDistanceToPole(n, z0, point) ?: 0.0 }.max()!!
                }
            }
        }

        return distance
    }

    /**
     * Get distance to the north/south pole from the given point.
     * Distance is measured by a curve inside a plane, that is determined by a given normal vector.
     * Pole is determined by "z = z0" equation in Cartesian coordinate system.
     *
     * @return distance to the pole or null, if pole will never be reached by this point in this plane.
     */
    private fun getDistanceToPole(n: CartesianVector, z0: Double, point: LatLng): Double? {
        val p = point.toCartesian()

        val A = n.x
        val B = n.y
        val D = -n.x * p.x - n.y * p.y

        val rho = abs(D) / sqrt(pow(A, 2.0) + pow(B, 2.0))
        val r = sqrt(pow(R, 2.0) - pow(rho, 2.0))

        if (abs(z0) > r) {
            return null
        }

        val phi1 = asin(p.z / r)
        val phi2 = asin(z0 / r)

        val distance = r * (phi2 - phi1)
        return abs(distance)
    }

    /**
     * Translate spherical coordinates to Cartesian.
     */
    private fun LatLng.toCartesian(): CartesianVector {
        val latitude = this.latitude.toRadians()
        val longitude = this.longitude.toRadians()
        return CartesianVector(
                x = R * cos(latitude) * cos(longitude),
                y = R * cos(latitude) * sin(longitude),
                z = R * sin(latitude)
        )
    }

    /**
     * Convert degrees to radians.
     */
    private fun Double.toRadians(): Double = this * PI / 180.0

    /**
     * Vector in 3-dimensional Cartesian coordinate system.
     */
    data class CartesianVector(val x: Double, val y: Double, val z: Double) {

        /**
         * Return normalized vector.
         */
        fun normalize(): CartesianVector {
            val denominator: Double = sqrt(pow(x, 2.0) + pow(y, 2.0) + pow(z, 2.0))
            return CartesianVector(x / denominator, y / denominator, z / denominator)
        }

    }

}