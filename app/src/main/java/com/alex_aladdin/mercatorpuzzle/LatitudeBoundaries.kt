package com.alex_aladdin.mercatorpuzzle

import com.alex_aladdin.google_maps_utils.MathUtil
import com.alex_aladdin.google_maps_utils.SphericalUtil
import com.mapbox.mapboxsdk.geometry.LatLng
import java.lang.Math.*

/**
 * Class that calculates allowable range of latitudes for Country's center.
 * It is also used to check whether point lies in this range.
 */
class LatitudeBoundaries(center: LatLng, coordinates: ArrayList<ArrayList<LatLng>>) {

    companion object {

        val MAX_LATITUDE = 85.06

    }

    private val R = MathUtil.EARTH_RADIUS
    private var centerMax = MAX_LATITUDE
    private var centerMin = -MAX_LATITUDE

    init {
        val n = findNormalVector(center)
        val z0 = R * sin(MAX_LATITUDE.toRadians())

        var minDistanceToNorth = Double.MAX_VALUE
        var minDistanceToSouth = Double.MAX_VALUE

        for (point in coordinates.flatten()) {
            getDistanceToPole(n, +z0, point)?.takeIf { it < minDistanceToNorth }?.let { minDistanceToNorth = it }
            getDistanceToPole(n, -z0, point)?.takeIf { it < minDistanceToSouth }?.let { minDistanceToSouth = it }
        }

        centerMax = SphericalUtil.computeOffset(center, minDistanceToNorth, 0.0).latitude
        centerMin = SphericalUtil.computeOffset(center, minDistanceToSouth, 180.0).latitude
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
    private fun Double.toRadians(): Double {
        return this * PI / 180.0
    }

    /**
     * Vector in 3-dimensional Cartesian coordinate system.
     */
    class CartesianVector(val x: Double, val y: Double, val z: Double) {

        /**
         * Return normalized vector.
         */
        fun normalize(): CartesianVector {
            val denominator: Double = sqrt(pow(x, 2.0) + pow(y, 2.0) + pow(z, 2.0))
            return CartesianVector(x / denominator, y / denominator, z / denominator)
        }

        override fun toString(): String {
            return "x = $x, y = $y, z = $z"
        }

    }

}