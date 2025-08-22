package com.dls.pymetask.data.location



import android.location.Location

/**
 * Abstracción mínima para obtener la ubicación actual.
 * La UI se encarga de solicitar permisos antes de invocar getCurrentLocation().
 */
interface LocationClient {
    /**
     * Intenta obtener la ubicación actual (rápida): primero getCurrentLocation,
     * si falla, prueba lastLocation. Puede devolver null.
     */
    suspend fun getCurrentLocation(): Location?
}
