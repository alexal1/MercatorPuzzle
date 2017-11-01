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
        val centers: Map<Country, PointF>,
        val scales: HashMap<Country, (PointF) -> PointF?>) : DrawThread("ScaleDrawThread", surfaceHolder) {

    companion object {

        fun obtain(surfaceHolder: SurfaceHolder, projection: Projection, countries: List<Country>): ScaleDrawThread {
            val points = HashMap<Country, List<List<PointF>>>(countries.size)
            val centers = HashMap<Country, PointF>()
            val scales = HashMap<Country, (PointF) -> PointF?>()

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

                centers[country] = projection.toScreenLocation(country.currentCenter)

                scales[country] = { null }
            }

            return ScaleDrawThread(surfaceHolder, points, centers, scales)
        }

    }

    override fun Canvas.drawFrame() {
        for ((country, countryPoints) in points) {
            val countryScale = scales[country]!!
            this.drawCountry(
                    country = countryPoints,
                    projection = countryScale
            )
        }
    }

}