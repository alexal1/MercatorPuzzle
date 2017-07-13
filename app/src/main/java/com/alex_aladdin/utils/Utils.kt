package com.alex_aladdin.utils

import android.graphics.PointF
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.services.commons.models.Position

/**
 * Transform LatLng object to Position object.
 */
fun LatLng.position(): Position {
    return Position.fromCoordinates(this.longitude, this.latitude)
}

/**
 * Euclidean distance between two points.
 */
fun PointF.distanceTo(point: PointF): Float {
    return Math.sqrt(Math.pow(point.x.toDouble() - this.x.toDouble(), 2.0)
            + Math.pow(point.y.toDouble() - this.y.toDouble(), 2.0)).toFloat()
}