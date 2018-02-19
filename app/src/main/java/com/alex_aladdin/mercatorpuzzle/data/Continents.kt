package com.alex_aladdin.mercatorpuzzle.data

import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.mapbox.mapboxsdk.geometry.LatLng

enum class Continents(val vertices: List<LatLng>, val center: LatLng, val count: Int, val stringId: Int) {

    EUROPE(
            vertices = listOf(
            LatLng(33.870415550941836, 33.92578125),
            LatLng(38.685509760012, 6.85546875),
            LatLng(35.88905007936091, -11.953125),
            LatLng(66.08936427047088, -26.19140625),
            LatLng(71.74643171904148, 26.54296875),
            LatLng(65.07213008560696, 51.50390625),
            LatLng(33.870415550941836, 33.92578125)),
            center =  LatLng(48.733333, 18.916667), // Kremnické Bane, Slovakia
            count = 42,
            stringId = R.string.continent_europe
    ),

    ASIA(
            vertices = emptyList(),
            center = LatLng(),
            count = 0,
            stringId = R.string.continent_asia
    ),

    AFRICA(
            vertices = emptyList(),
            center = LatLng(),
            count = 0,
            stringId = R.string.continent_africa
    ),

    NORTH_AMERICA(
            vertices = emptyList(),
            center = LatLng(),
            count = 0,
            stringId = R.string.continent_north_america
    ),

    SOUTH_AMERICA(
            vertices = emptyList(),
            center = LatLng(),
            count = 0,
            stringId = R.string.continent_south_america
    ),

    OCEANIA(
            vertices = emptyList(),
            center = LatLng(),
            count = 0,
            stringId = R.string.continent_oceania
    );

    fun toCountry(): Country = Country(
            vertices = arrayListOf(ArrayList(this@Continents.vertices)),
            id = this@Continents.toString().take(3),
            name = this@Continents.toString()
    )

}