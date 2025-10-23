package com.example.casinoapp.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Objeto encargado de obtener la ubicación del usuario de manera segura y eficiente.
 * Utiliza la API de FusedLocationProvider (Google Play Services).
 *
 * Incluye estrategias de fallback, manejo de permisos y timeout.
 */
object LocationHelper {

    /**
     * Verifica si la app tiene permisos de ubicación (fina o aproximada).
     * Retorna true si al menos uno está concedido.
     */
    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Crea una instancia del proveedor de ubicación fusionada (API de Google).
     */
    private fun fused(context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la mejor ubicación posible aplicando una estrategia escalonada:
     *
     * 1️⃣ `lastLocation`: intenta obtener la última ubicación conocida (rápido, pero puede estar desactualizado).
     * 2️⃣ `getCurrentLocation`: solicita una lectura nueva con alta precisión.
     * 3️⃣ Si las dos anteriores fallan, pide actualizaciones de ubicación y espera el **primer resultado válido** dentro de un tiempo límite (`timeoutMs`).
     *
     * Si no hay permisos o no se obtiene ninguna ubicación, devuelve `null`.
     */
    @SuppressLint("MissingPermission")
    suspend fun getBestLocation(context: Context, timeoutMs: Long = 8000L): Location? {
        if (!hasPermission(context)) return null

        // (1) Última ubicación conocida (caché)
        fused(context).lastLocation.await()?.let { return it }

        // (2) Solicitud puntual de ubicación actual con alta precisión
        val token = CancellationTokenSource().token
        fused(context).getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token).await()?.let { return it }

        // (3) Esperar el primer update válido (timeout configurable)
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, /*interval*/ 1000L)
            .setMinUpdateIntervalMillis(800L)
            .setMaxUpdateDelayMillis(1500L)
            .build()

        return withTimeoutOrNull(timeoutMs) {
            awaitFirstLocationUpdate(context, req)
        }
    }

    /**
     * Alias para compatibilidad: simplemente llama a getBestLocation().
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? =
        getBestLocation(context)

    /**
     * Convierte coordenadas (latitud y longitud) en el nombre de una ciudad o región.
     * Utiliza la clase `Geocoder` de Android con idioma español para Chile ("es-CL").
     * Si no encuentra resultados, retorna null.
     */
    @Suppress("DEPRECATION")
    suspend fun resolveCity(context: Context, lat: Double, lon: Double): String? {
        return runCatching {
            // Intenta obtener dirección inversa (reverse geocoding)
            val geocoder = Geocoder(context, Locale("es", "CL"))
            val list: List<Address> = geocoder.getFromLocation(lat, lon, 1) ?: emptyList()
            if (list.isEmpty()) null else {
                val a = list.first()
                val city = a.locality ?: a.subAdminArea ?: a.subLocality
                val region = a.adminArea ?: a.subAdminArea
                // Combina ciudad y región en una sola cadena
                listOfNotNull(city, region).joinToString(", ")
            }
        }.getOrNull()
    }

    // --- Funciones privadas auxiliares ---

    /**
     * Espera el primer resultado válido de `requestLocationUpdates`.
     * Usa corrutinas para suspender hasta que se obtenga una ubicación o se cancele.
     */
    private suspend fun awaitFirstLocationUpdate(
        context: Context,
        request: LocationRequest
    ): Location? = suspendCancellableCoroutine { cont ->
        val f = fused(context)
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null && cont.isActive) {
                    // Retorna la ubicación y detiene las actualizaciones
                    cont.resume(loc)
                    f.removeLocationUpdates(this)
                }
            }
        }
        // Inicia la escucha de actualizaciones
        f.requestLocationUpdates(request, cb, context.mainLooper)
        // Si la corrutina se cancela, se detienen las actualizaciones
        cont.invokeOnCancellation { f.removeLocationUpdates(cb) }
    }
}

/**
 * Extensión para convertir un `Task<T>` (API de Google) a una función suspendida.
 * Permite usar `await()` con corrutinas sin librerías externas.
 */
private suspend fun <T> Task<T>.await(): T? = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) } // Éxito
    addOnFailureListener { cont.resume(null) } // Error o null
}
