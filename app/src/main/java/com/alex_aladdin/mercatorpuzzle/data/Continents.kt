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
            vertices = listOf(
                    LatLng(33.12828445238249, 33.45952166954635),
                    LatLng(9.524835707476441, 54.28959979454635),
                    LatLng(-11.016766749358556, 113.70366229454635),
                    LatLng(25.948094628866407, 142.18022479454635),
                    LatLng(56.407779277135724, 131.63334979454635),
                    LatLng(56.212769753368086, 60.96928729454635),
                    LatLng(33.12828445238249, 33.45952166954635)
            ),
            center = LatLng(43.681111, 87.331111),  // Ürümqi, Xinjiang province, China
            count = 45,
            stringId = R.string.continent_asia
    ),

    AFRICA(
            vertices = listOf(
                    LatLng(32.85395382523863, 29.69244743689319),
                    LatLng(1.2437980987720791, 48.14947868689319),
                    LatLng(-8.219948225686775, 42.34869743689319),
                    LatLng(-12.369813322097805, 53.42291618689319),
                    LatLng(-27.047167853882566, 49.55572868689319),
                    LatLng(-38.400036330123356, 18.96979118689319),
                    LatLng(-17.29586842365752, 7.89557243689319),
                    LatLng(0.36498708242583244, 5.96197868689319),
                    LatLng(6.153904627996047, -21.98724006310681),
                    LatLng(31.814303077302895, -17.94427131310681),
                    LatLng(37.45063424607954, 4.37994743689319),
                    LatLng(32.85395382523863, 29.69244743689319)
            ),
            center = LatLng(5.65, 26.17),           // Obo, Central African Republic
            count = 51,
            stringId = R.string.continent_africa
    ),

    NORTH_AMERICA(
            vertices = listOf(
                    LatLng(60.22138887837598, -39.04574823474576),
                    LatLng(69.88736283112377, -19.534029484745815),
                    LatLng(84.08505521238203, -4.416841984745815),
                    LatLng(84.10314220780712, -132.73715448474576),
                    LatLng(6.802328690613256, -132.91293573474576),
                    LatLng(6.453116224842353, -81.40902948474576),
                    LatLng(14.908933384133952, -73.49887323474576),
                    LatLng(9.932305217027578, -54.69027948474576),
                    LatLng(60.22138887837598, -39.04574823474576)
            ),
            center = LatLng(48.367222, -99.996111), // Rugby, North Dakota, USA
            count = 18,
            stringId = R.string.continent_north_america
    ),

    SOUTH_AMERICA(
            vertices = listOf(
                    LatLng(-57.13283144829155, -74.67071849353596),
                    LatLng(1.763813688349708, -94.09454661853596),
                    LatLng(10.061586056994734, -62.98126536853596),
                    LatLng(-5.959507804037502, -32.13165599353596),
                    LatLng(-55.674043367038294, -34.68048411853596),
                    LatLng(-57.13283144829155, -74.67071849353596)
            ),
            center = LatLng(-15.595833, -56.096944),// Cuiabá, Brazil
            count = 14,
            stringId = R.string.continent_south_america
    ),

    OCEANIA(
            vertices = listOf(
                    LatLng(6.120663375668458, 178.76900508607457),
                    LatLng(-52.60185299143187, 178.76900508607457),
                    LatLng(-38.426232690191014, 111.79634883607457),
                    LatLng(-18.665199811521322, 106.69869258607457),
                    LatLng(6.120663375668458, 178.76900508607457)
            ),
            center = LatLng(-25.610111, 134.354806),// Lambert gravitational centre, Australia
            count = 8,
            stringId = R.string.continent_oceania
    );

    fun toCountry(): Country = Country(
            vertices = arrayListOf(ArrayList(this@Continents.vertices)),
            id = this@Continents.toString().take(3),
            name = this@Continents.toString()
    )

}