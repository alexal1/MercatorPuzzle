package com.alex_aladdin.mercatorpuzzle

import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.services.commons.models.Position
import com.mapbox.services.commons.turf.TurfConstants
import com.mapbox.services.commons.turf.TurfMeasurement
import java.lang.Math.*

/**
 * Spherical geometry functions.
 * This class helps to perform rotation of any point on the Earth's surface.
 * Rotation is initialized in the constructor by two geographic coordinates. Movement from first
 * coordinate to second is considered as rotation in great circle's plane, which is drawn through
 * both of these points.
 */
class SphericalUtil(val from: LatLng, val to: LatLng) {

    private val LOG_TAG = "MercatorSphericalUtil"
    val R = 1.0
    var rotationMatrix: RotationMatrix? = null

    init {
        val a = from.toCartesian()
        val b = to.toCartesian()
        val n = CartesianVector(
                x = a.y * b.z - a.z * b.y,
                y = a.z * b.x - a.x * b.z,
                z = a.x * b.y - a.y * b.x
        ).normalize()

        val theta = TurfMeasurement.distance(
                Position.fromCoordinates(from.longitude, from.latitude),
                Position.fromCoordinates(to.longitude, to.latitude),
                TurfConstants.UNIT_RADIANS
        )

        Log.i(LOG_TAG, "theta = ${theta.toDegrees()}")
        Log.i(LOG_TAG, "n = ($n)")

        rotationMatrix = RotationMatrix(n, theta)
    }

    /**
     * Apply rotation to the given coordinates.
     *
     * @return new coordinates or null, if rotation matrix is not initialized
     */
    fun getNewCoordinates(latLng: LatLng): LatLng? {
        rotationMatrix ?: return null
        val old = latLng.toCartesian()
        val new = rotationMatrix!!.multiply(old)
        return new.toSpherical()
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
     * Translate Cartesian coordinates to spherical.
     */
    private fun CartesianVector.toSpherical(): LatLng {
        return LatLng(asin(this.z / R).toDegrees(), atan2(this.y, this.x).toDegrees())
    }

    /**
     * Convert radians to degrees.
     */
    private fun Double.toDegrees(): Double {
        return this * 180.0 / PI
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

    /**
     * Rotation matrix initialized by unit vector and rotation angle.
     */
    class RotationMatrix(u: CartesianVector, theta: Double) {

        /**
         * Implementation of Double matrix.
         */
        private fun matrix(sizeOuter: Int, sizeInner: Int): Array<DoubleArray>
                = Array(sizeOuter) { DoubleArray(sizeInner) }

        val M = matrix(3, 3)  // Matrix itself

        init {
            M[0][0] = cos(theta) + pow(u.x, 2.0) * (1 - cos(theta))
            M[0][1] = u.x * u.y * (1 - cos(theta)) - u.z * sin(theta)
            M[0][2] = u.x * u.z * (1 - cos(theta)) + u.y * sin(theta)
            M[1][0] = u.y * u.x * (1 - cos(theta)) + u.z * sin(theta)
            M[1][1] = cos(theta) + pow(u.y, 2.0) * (1 - cos(theta))
            M[1][2] = u.y * u.z * (1 - cos(theta)) - u.x * sin(theta)
            M[2][0] = u.z * u.x * (1 - cos(theta)) - u.y * sin(theta)
            M[2][1] = u.z * u.y * (1 - cos(theta)) + u.x * sin(theta)
            M[2][2] = cos(theta) + pow(u.z, 2.0) * (1 - cos(theta))
        }

        /**
         * Matrix product of rotation matrix and given vector.
         */
        fun multiply(v: CartesianVector): CartesianVector {
            return CartesianVector(
                    x = M[0][0]*v.x + M[0][1]*v.y + M[0][2]*v.z,
                    y = M[1][0]*v.x + M[1][1]*v.y + M[1][2]*v.z,
                    z = M[2][0]*v.x + M[2][1]*v.y + M[2][2]*v.z)
        }

    }

}