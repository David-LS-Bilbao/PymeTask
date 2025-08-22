package com.dls.pymetask.presentation.weather


/**
 * Mapea el código WMO (World Meteorological Organization) a una clave simple
 * que después convertiremos en icono + texto localizado.
 *
 * Claves: "sunny", "cloudy", "rain", "snow", "storm", "haze"
 * (Se pueden afinar más adelante.)
 */
fun wmoToKey(code: Int): String = when (code) {
    0 -> "sunny"                 // Despejado
    1,2,3 -> "cloudy"            // Poco/Parcialmente/Muy nuboso
    45,48 -> "haze"              // Niebla/escarcha
    51,53,55,61,63,65,80,81,82 -> "rain"   // Llovizna/Lluvia/chubascos
    71,73,75,85,86 -> "snow"     // Nieve
    95,96,99 -> "storm"          // Tormenta
    else -> "cloudy"
}
