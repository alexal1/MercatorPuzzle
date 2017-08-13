package com.alex_aladdin.mercatorpuzzle

import android.graphics.*
import android.util.Log
import com.alex_aladdin.utils.distanceTo
import com.alex_aladdin.utils.google_maps_utils.PolyUtil
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * This class represents one draggable country.
 * Country's vertices are stored as a list of polygons, and each polygon is a list of coordinates.
 */
class Country(private var vertices: ArrayList<ArrayList<LatLng>>, val id: String, val name: String) {

    val LOG_TAG = "MercatorCountry"
    val targetCenter = getCenter()
    private val relativeVertices = RelativeVertices(center = targetCenter, coordinates = vertices)
    private val latitudeBoundaries = LatitudeBoundaries(center = targetCenter, coordinates = vertices)
    private val polygonsOnMap: ArrayList<Polygon> = ArrayList()

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
     * Draw this country on canvas.
     *
     * @param canvas        canvas object for drawing
     * @param projection    function that calculates screen point for given geographic coordinate
     */
    fun drawOnCanvas(canvas: Canvas, projection: (LatLng) -> PointF) {
        val paint = Paint()
        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE

        // Draw one polygon on canvas
        fun drawPolygon(polygon: ArrayList<LatLng>) {
            val path = Path()
            val pointStart: PointF = projection(polygon[0])
            path.moveTo(pointStart.x, pointStart.y)

            // Part of polygon that got out to the opposite part of the screen
            val cutPolygon = ArrayList<LatLng>()
            // This flag shows if we if we are going through cutPolygon's points or not
            var startCutPolygon = false
            // Previous point
            var prevPoint: PointF = pointStart
            // Half of screen width
            val halfScreen = MercatorApp.screen.x / 2
            // Go through all points
            (1..polygon.size-1).forEach { i ->
                val point: PointF = projection(polygon[i])

                if (point.distanceTo(prevPoint) > halfScreen) {
                    startCutPolygon = !startCutPolygon
                }

                if (startCutPolygon) {
                    cutPolygon.add(polygon[i])
                }
                else {
                    path.lineTo(point.x, point.y)
                }

                prevPoint = point
            }

            path.close()
            canvas.drawPath(path, paint)

            // Recursive call to draw cutPolygon
            if (cutPolygon.isNotEmpty()) {
                drawPolygon(cutPolygon)
            }
        }

        for (polygon in vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(LOG_TAG, "Incorrect polygon in $name!")
                continue
            }

            drawPolygon(polygon)
        }
    }

    /**
     * Draw this country on map.
     *
     * @param mapboxMap map object for drawing
     */
    fun drawOnMap(mapboxMap: MapboxMap) {
        for (polygon in vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(LOG_TAG, "Incorrect polygon in $name!")
                continue
            }

            val polygonOptions = PolygonOptions()
                    .fillColor(Color.parseColor("#ff0000"))
                    .addAll(polygon)
            val newPolygon = mapboxMap.addPolygon(polygonOptions)
            polygonsOnMap.add(newPolygon)
        }
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

    /**
     * Remove country from the map.
     */
    fun removeFromMap() {
        if (polygonsOnMap.isNotEmpty()) {
            polygonsOnMap.forEach { it.remove() }
            polygonsOnMap.clear()
        }
    }

}