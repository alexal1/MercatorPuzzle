package com.alex_aladdin.mercatorpuzzle

import android.graphics.*
import android.util.Log
import com.mapbox.mapboxsdk.annotations.Polygon
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.commons.models.Position
import com.mapbox.services.commons.turf.TurfJoins

/**
 * This class represents one draggable country.
 * Country's vertices are stored as a list of polygons, and each polygon is a list of coordinates.
 */
class Country(private val vertices: ArrayList<ArrayList<LatLng>>, val id: String, val name: String) {

    val LOG_TAG = "MercatorCountry"
    private var lastFixedVertices = getVertices()               // Vertices are being fixed on drop
    private val polygonsOnMap: ArrayList<Polygon> = ArrayList() // Saved objects of polygons allow us to remove them

    /**
     * Save current vertices into a separate instance.
     */
    private fun getVertices(): ArrayList<ArrayList<LatLng>> {
        val newVertices: ArrayList<ArrayList<LatLng>> = ArrayList()
        vertices.forEachIndexed { i, polygon ->
            newVertices.add(i, ArrayList<LatLng>())
            polygon.forEachIndexed { j, latLng ->
                newVertices[i].add(j, latLng)
            }
        }
        return newVertices
    }

    /**
     * Update all country's vertices by transforming last fixed vertices.
     *
     * @param updateFunction function that transforms coordinates
     */
    fun updateVertices(updateFunction: (LatLng) -> LatLng) {
        lastFixedVertices.forEachIndexed { i, polygon ->
            polygon.forEachIndexed { j, latLng ->
                vertices[i][j] = updateFunction(latLng)
            }
        }
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

        // Fix vertices
        lastFixedVertices = getVertices()
    }

    /**
     * Calculate country's center.
     */
    fun center(): LatLng {
        var minLat: Double = vertices[0][0].latitude
        var maxLat: Double = vertices[0][0].latitude
        var minLng: Double = vertices[0][0].longitude
        var maxLng: Double = vertices[0][0].longitude

        for (polygon in vertices) {
            for (latLng in polygon) {
                if (latLng.latitude < minLat) minLat = latLng.latitude
                if (latLng.latitude > maxLat) maxLat = latLng.latitude
                if (latLng.longitude < minLng) minLng = latLng.longitude
                if (latLng.longitude > maxLng) maxLng = latLng.longitude
            }
        }

        return LatLng((minLat + maxLat) / 2.0, (minLng + maxLng) / 2.0)
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