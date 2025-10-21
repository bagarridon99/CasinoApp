package com.example.casinoapp.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun fused(context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Estrategia robusta:
     * 1) lastLocation (rápido/caché)
     * 2) currentLocation High Accuracy
     * 3) requestLocationUpdates y esperamos el PRIMER fix con timeout
     */
    @SuppressLint("MissingPermission")
    suspend fun getBestLocation(context: Context, timeoutMs: Long = 8000L): Location? {
        if (!hasPermission(context)) return null

        // 1) lastLocation
        fused(context).lastLocation.await()?.let { return it }

        // 2) currentLocation (con token de cancelación)
        val token = CancellationTokenSource().token
        fused(context).getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token).await()?.let { return it }

        // 3) Esperar primer update con timeout
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, /*interval*/ 1000L)
            .setMinUpdateIntervalMillis(800L)
            .setMaxUpdateDelayMillis(1500L)
            .build()

        return withTimeoutOrNull(timeoutMs) {
            awaitFirstLocationUpdate(context, req)
        }
    }

    // Alias para compatibilidad con código existente
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? =
        getBestLocation(context)


    @Suppress("DEPRECATION")
    suspend fun resolveCity(context: Context, lat: Double, lon: Double): String? {
        return runCatching {
            // Usa "es-CL"; si no hay datos, cae a Locale por defecto.
            val geocoder = Geocoder(context, Locale("es", "CL"))
            val list: List<Address> = geocoder.getFromLocation(lat, lon, 1) ?: emptyList()
            if (list.isEmpty()) null else {
                val a = list.first()
                val city = a.locality ?: a.subAdminArea ?: a.subLocality
                val region = a.adminArea ?: a.subAdminArea
                listOfNotNull(city, region).joinToString(", ")
            }
        }.getOrNull()
    }

    // --- Helpers privados ---

    private suspend fun awaitFirstLocationUpdate(
        context: Context,
        request: LocationRequest
    ): Location? = suspendCancellableCoroutine { cont ->
        val f = fused(context)
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null && cont.isActive) {
                    cont.resume(loc)
                    f.removeLocationUpdates(this)
                }
            }
        }
        f.requestLocationUpdates(request, cb, context.mainLooper)
        cont.invokeOnCancellation { f.removeLocationUpdates(cb) }
    }
}

/* await para Task<> sin libs extra */
private suspend fun <T> Task<T>.await(): T? = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resume(null) }
}
