package com.example.casinoapp.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.casinoapp.location.LocationHelper
import kotlinx.coroutines.launch

/**
 * Tarjeta que:
 * 1) Pide permisos de ubicación (cuando no están concedidos).
 * 2) Intenta resolver la ciudad actual del usuario (geocoder).
 * 3) Si hay ciudad, permite aplicar un bono del +20% sobre un monto base.
 *
 * @param currentAmount Monto actual de referencia (se usa para calcular el bono)
 * @param onApplyBonus Callback para aplicar el bono calculado (en pesos)
 * @param onCityResolved Callback que recibe la ciudad detectada (o null si falla)
 * @param modifier Modificador opcional para el contenedor
 */
@Composable
fun CityBonusCard(
    currentAmount: Int,
    onApplyBonus: (Int) -> Unit,
    onCityResolved: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado local: permisos, estado textual, ciudad resuelta y loading
    var hasPermission by remember { mutableStateOf(LocationHelper.hasPermission(ctx)) }
    var status by remember { mutableStateOf<String>("Ubicación no solicitada") }
    var city by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Launcher para pedir múltiples permisos de ubicación (fine + coarse)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val granted = (map[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (map[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        hasPermission = granted
        status = if (granted) "Permiso concedido" else "Permiso denegado"
    }

    // Función local: solicita permisos al sistema
    fun request() {
        launcher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // Función suspendida: intenta obtener ubicación y resolver ciudad con geocoder
    suspend fun resolve() {
        loading = true
        status = "Obteniendo ubicación…"
        val loc = LocationHelper.getCurrentLocation(ctx) // usa estrategia de best-effort
        if (loc != null) {
            status = "Ubicación lista"
            val c = LocationHelper.resolveCity(ctx, loc.latitude, loc.longitude)
            city = c
            onCityResolved(c) // informa a quien consume este composable
        } else {
            status = "No se pudo obtener ubicación"
        }
        loading = false
    }

    // Contenedor visual: superficie elevada y con esquinas redondeadas del theme
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Bono por ciudad", style = MaterialTheme.typography.titleLarge)
            Text(
                // Muestra ciudad si existe; si no, el estado (permiso/obteniendo/error)
                when {
                    city != null -> "Estás en: $city"
                    else -> status
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!hasPermission) {
                    // Botón para pedir permisos si aún no están
                    Button(onClick = ::request, enabled = !loading) { Text("Pedir permiso") }
                } else {
                    // Botón para detectar ubicación cuando ya hay permisos
                    OutlinedButton(
                        onClick = { scope.launch { resolve() } },
                        enabled = !loading
                    ) { Text("Detectar ubicación") }
                }

                // Si hay ciudad resuelta, habilita aplicar bono +20%
                if (city != null) {
                    val base = currentAmount.coerceAtLeast(100) // mínimo de seguridad
                    val bonus = (base * 20) / 100
                    Button(onClick = { onApplyBonus(bonus) }) {
                        Text("+20% ($$bonus)")
                    }
                }
            }
        }
    }
}
