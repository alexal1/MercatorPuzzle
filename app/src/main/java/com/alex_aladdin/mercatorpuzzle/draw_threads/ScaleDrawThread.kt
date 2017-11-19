package com.alex_aladdin.mercatorpuzzle.draw_threads

import android.graphics.Canvas
import android.graphics.PointF
import android.view.SurfaceHolder
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.mapbox.mapboxsdk.maps.Projection

/**
 * ScaleDrawThread is used for several Countries' simultaneous scaling.
 */
class ScaleDrawThread private constructor(
        surfaceHolder: SurfaceHolder,
        private val points: Map<Country, List<List<PointF>>>,
        private val colors: Map<Country, Int>,
        val centers: Map<Country, PointF>,
        val scales: HashMap<Country, Scale>) : DrawThread("ScaleDrawThread", surfaceHolder) {

    companion object {

        fun obtain(surfaceHolder: SurfaceHolder, projection: Projection, countries: List<Country>): ScaleDrawThread {
            val points = HashMap<Country, List<List<PointF>>>(countries.size)
            val colors = HashMap<Country, Int>()
            val centers = HashMap<Country, PointF>()
            val scales = HashMap<Country, Scale>()

            countries.forEach { country ->
                val countryPoints = ArrayList<ArrayList<PointF>>()
                country.vertices.forEachIndexed { i, polygon ->
                    countryPoints.add(i, ArrayList())
                    polygon.forEachIndexed { j, vertex ->
                        val point: PointF = projection.toScreenLocation(vertex)
                        countryPoints[i].add(j, point)
                    }
                }
                points[country] = countryPoints

                colors[country] = country.color

                centers[country] = projection.toScreenLocation(country.currentCenter)

                scales[country] = Scale()
            }

            return ScaleDrawThread(surfaceHolder, points, colors, centers, scales)
        }

    }

    override fun Canvas.drawFrame() {
        for ((country, countryPoints) in points) {
            val countryScale = scales[country]!!
            val countryColor = colors[country]!!
            this.drawCountry(
                    country = countryPoints,
                    projection = countryScale.scaleMapping,
                    color = countryColor,
                    distanceLimit = countryScale.scaleFactor * DEFAULT_DISTANCE_LIMIT
            )
        }
    }

    data class Scale(val scaleFactor: Float, val scaleMapping: (PointF) -> PointF?) {

        constructor() : this(0f, { null })

    }

}