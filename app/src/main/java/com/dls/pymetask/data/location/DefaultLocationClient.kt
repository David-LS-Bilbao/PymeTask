package com.dls.pymetask.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

class DefaultLocationClient(
    private val app: Application,
    private val fused: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission")
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getCurrentLocation(): Location? {

        Log.d("LocationClient", "getCurrentLocation() -> start")

        val pri = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        Log.d("LocationClient", "priority=$pri (BALANCED)")



        val current = runCatching {
            suspendCancellableCoroutine<Location?> { cont ->
                fused.getCurrentLocation(pri, /* token */ null)
                    .addOnSuccessListener { loc ->
                        Log.d("LocationClient", "getCurrentLocation -> $loc")
                        cont.resume(loc)
                    }
                    .addOnFailureListener { e ->
                        Log.w("LocationClient", "getCurrentLocation FAILED: ${e.message}")
                        cont.resume(null)
                    }
            }
        }.getOrNull()

        if (current != null) return current.also {
            Log.d("LocationClient", "return current=${it.latitude},${it.longitude}")
        }

        val last = runCatching {
            suspendCancellableCoroutine<Location?> { cont ->
                fused.lastLocation
                    .addOnSuccessListener { loc ->
                        Log.d("LocationClient", "lastLocation -> $loc")
                        cont.resume(loc)
                    }
                    .addOnFailureListener { e ->
                        Log.w("LocationClient", "lastLocation FAILED: ${e.message}")
                        cont.resume(null)
                    }
            }
        }.getOrNull()

        Log.d("LocationClient", "return last=$last")
        return last

//
//        val hasFine = ContextCompat.checkSelfPermission(
//            app, Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//        val priority = if (hasFine) {
//            Priority.PRIORITY_HIGH_ACCURACY
//        } else {
//            Priority.PRIORITY_BALANCED_POWER_ACCURACY
//        }
//
//        val tokenSrc = CancellationTokenSource()
//
//        return try {
//            // 1) Intento rápido con timeout corto
//            val current: Location? = try {
//                withTimeout(2500) {
//                    fused.getCurrentLocation(priority, tokenSrc.token).awaitNullable()
//                }
//            } catch (t: TimeoutCancellationException) {
//                null
//            }
//
//            if (current != null) current
//            else fused.lastLocation.awaitNullable()
//        } catch (se: SecurityException) {
//            null
//        } finally {
//            tokenSrc.cancel()
//        }
    }
}

/** Helper para await sin lanzar si el Task devuelve null */
private suspend fun <T> Task<T>.awaitNullable(): T? =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resume(null) }
        addOnCanceledListener { cont.resume(null) } // si el Task se cancela, devolvemos null

    }
