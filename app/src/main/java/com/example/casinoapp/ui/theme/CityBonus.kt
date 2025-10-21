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
 * - Muestra la ciudad ("Estás en: ...")
 * - Si hay ciudad, permite reclamar un bono del +20% del monto actual (o del mínimo si no se editó).
 * Devuelve la ciudad encontrada vía onCityResolved.
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

    var hasPermission by remember { mutableStateOf(LocationHelper.hasPermission(ctx)) }
    var status by remember { mutableStateOf<String>("Ubicación no solicitada") }
    var city by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val granted = (map[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (map[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        hasPermission = granted
        status = if (granted) "Permiso concedido" else "Permiso denegado"
    }

    fun request() {
        launcher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    suspend fun resolve() {
        loading = true
        status = "Obteniendo ubicación…"
        val loc = LocationHelper.getCurrentLocation(ctx)
        if (loc != null) {
            status = "Ubicación lista"
            val c = LocationHelper.resolveCity(ctx, loc.latitude, loc.longitude)
            city = c
            onCityResolved(c)
        } else {
            status = "No se pudo obtener ubicación"
        }
        loading = false
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Bono por ciudad", style = MaterialTheme.typography.titleLarge)
            Text(
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
                    Button(onClick = ::request, enabled = !loading) { Text("Pedir permiso") }
                } else {
                    OutlinedButton(
                        onClick = { scope.launch { resolve() } },
                        enabled = !loading
                    ) { Text("Detectar ubicación") }
                }

                // Si tenemos ciudad, permitimos aplicar bono +20%
                if (city != null) {
                    val base = currentAmount.coerceAtLeast(100)
                    val bonus = (base * 20) / 100
                    Button(onClick = { onApplyBonus(bonus) }) {
                        Text("+20% ($$bonus)")
                    }
                }
            }
        }
    }
}
