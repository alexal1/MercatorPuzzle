package com.alex_aladdin.mercatorpuzzle

import android.os.AsyncTask
import android.util.Log
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.IOException

/**
 * Parses GeoJSON data into Country objects by their IDs. Does it in a background thread.
 *
 * @param completion callback function to invoke after task execution
 */
class GeoJsonParser(val completion: (countries: List<Country>) -> Unit) : AsyncTask<String, Void, List<Country>>() {

    private val LOG_TAG = "MercatorGeoJsonParser"

    override fun onPostExecute(result: List<Country>?) {
        super.onPostExecute(result)

        if (result == null)
            Log.e(LOG_TAG, "Task failed!")
        else
            completion(result)
    }

    override fun doInBackground(vararg ids: String?): List<Country> {
        val jsonString = loadJsonFromAssets() ?: return emptyList()
        val gson = Gson()
        val type = object : TypeToken<GeoJsonStructure>() {}.type
        val json: GeoJsonStructure = gson.fromJson(jsonString, type)

        val result = ArrayList<Country>()
        json.features.forEach { country ->
            ids.find { it == country.id }?.apply {
                parseCountry(country)?.let {
                    result.add(it)
                }
            }
        }

        return result
    }

    /**
     * Read file containing GeoJSON data.
     */
    private fun loadJsonFromAssets(): String? {
        val json: String
        try {
            val inputStream = MercatorApp.applicationContext.assets.open("countries.geo.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        }
        catch (e: IOException) {
            Log.e(LOG_TAG, e.toString())
            return null
        }
        return json
    }

    /**
     * Obtain Country from GeoJSON object.
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseCountry(jsonCountry: GeoJsonStructure.GeoJsonCountry): Country? {
        // Utility function that obtains only one polygon
        fun parsePolygon(jsonPolygon: List<List<List<Double>>>): ArrayList<LatLng> {
            // GeoJSON polygon is a list of linear rings, we need only first one (no holes).
            // Each linear ring is a list of coordinates, each coordinate is a pair of numbers.
            return ArrayList(
                    jsonPolygon.first().map { pair -> LatLng(pair.component2(), pair.component1()) }
            )
        }

        val vertices: ArrayList<ArrayList<LatLng>>
        if (jsonCountry.geometry.type == "Polygon") {
            vertices = arrayListOf(
                    parsePolygon(jsonCountry.geometry.coordinates as List<List<List<Double>>>)
            )
        }
        else if (jsonCountry.geometry.type == "MultiPolygon") {
            vertices = ArrayList(
                    (jsonCountry.geometry.coordinates as List<List<List<List<Double>>>>).map {
                        parsePolygon(it)
                    }
            )
        }
        else {
            Log.e(LOG_TAG, "Unknown GeoJSON geometry in ${jsonCountry.properties.name}")
            return null
        }

        return Country(vertices = vertices, id = jsonCountry.id, name = jsonCountry.properties.name)
    }

    /**
     * Class that represents GeoJSON data structure.
     */
    private class GeoJsonStructure(val features: List<GeoJsonCountry>) {

        class GeoJsonCountry(val id: String, val properties: Properties, val geometry: Geometry) {

            class Properties(val name: String)

            class Geometry(val type: String, val coordinates: Any)

        }
    }

}