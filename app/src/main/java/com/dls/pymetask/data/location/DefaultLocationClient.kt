package com.dls.pymetask.data.location


import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implementación por defecto usando FusedLocationProviderClient.
 * Importante: asume que los permisos han sido concedidos por la UI.
 */
class DefaultLocationClient(
    private val app: Application,
    private val fused: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission")
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getCurrentLocation(): Location? {
        // 1) Intento rápido con getCurrentLocation (HIGH_ACCURACY si hay permiso FINE, si no COARSE)
        val pri = Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val current = runCatching {
            suspendCancellableCoroutine<Location?> { cont ->
                fused.getCurrentLocation(pri, /* cancellationToken */ null)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }.getOrNull()

        if (current != null) return current

        // 2) Fallback a lastLocation
        val last = runCatching {
            suspendCancellableCoroutine<Location?> { cont ->
                fused.lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }.getOrNull()

        return last
    }
}
