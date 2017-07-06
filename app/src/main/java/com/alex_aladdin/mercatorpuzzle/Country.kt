package com.alex_aladdin.mercatorpuzzle

import android.graphics.*
import android.util.Log
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.api.utils.turf.TurfJoins
import com.mapbox.services.commons.models.Position

/**
 * This class represents one draggable country.
 * Country's vertices are stored as a list of polygons, and each polygon is a list of coordinates.
 */
class Country(private var vertices: ArrayList<ArrayList<LatLng>>, val id: String, val name: String) {

    val LOG_TAG = "MercatorCountry"
    val targetCenter = getCenter()
    private val relativeVertices = RelativeVertices(center = targetCenter, coordinates = vertices)
    private val polygonsOnMap: ArrayList<Polygon> = ArrayList()

    /**
     * Update all Country's vertices by moving them according to the new Country's center.
     *
     * @param newCenter coordinates of a new Country's center
     */
    fun updateVertices(newCenter: LatLng) {
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

        for (polygon in vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(LOG_TAG, "Incorrect polygon in $name!")
                continue
            }

            val path = Path()
            val pointStart: PointF = projection(polygon[0])
            path.moveTo(pointStart.x, pointStart.y)

            (1..polygon.size-1).forEach { i ->
                val point: PointF = projection(polygon[i])
                path.lineTo(point.x, point.y)
            }

            path.close()
            canvas.drawPath(path, paint)
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
        val distances: ArrayList<Double> = ArrayList()
        (0..n-1).forEach { i ->
            distances.add(i, longitudes[i+1] - longitudes[i])
        }
        distances.add(n, 360.0 - longitudes[n] + longitudes[0])
        val maxDistance: Int = distances.indices.maxBy { distances[it] } ?: -1

        // Center longitude is a half-sum of country's longitude boundaries
        val centerLng: Double = (longitudes[maxDistance] + longitudes[(maxDistance + 1) % n]) / 2
        if (this.contains(LatLng(centerLat, centerLng))) {
            return LatLng(centerLat, centerLng)
        }
        else {
            return LatLng(centerLat, (centerLng + 180.0) % 360.0)
        }
    }

    /**
     * Check if country contains given point or not.
     */
    fun contains(latLng: LatLng): Boolean {
        var contains = false
        for (polygon in vertices) {
            val polygonPositions: ArrayList<Position> = arrayListOf()
            polygon.mapTo(polygonPositions, { point ->
                Position.fromCoordinates(point.longitude, point.latitude)
            })

            if (TurfJoins.inside(Position.fromCoordinates(latLng.longitude, latLng.latitude), polygonPositions)) {
                contains = true
            }
        }

        return contains
    }

    /**
     * Remove country from the map.
     */
    fun removeFromMap() {
        polygonsOnMap.forEach { it.remove() }
        polygonsOnMap.clear()
    }

}