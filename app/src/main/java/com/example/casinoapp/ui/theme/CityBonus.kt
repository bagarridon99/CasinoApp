package com.example.casinoapp.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.location.LocationHelper
import kotlinx.coroutines.launch

// Colores locales para asegurar consistencia con el Home
private val DarkBg = Color(0xFF333333)
private val Gold = Color(0xFFFFD700)

@Composable
fun CityBonusCard(
    currentAmount: Int,
    onApplyBonus: (Int) -> Unit,
    onCityResolved: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado local
    var hasPermission by remember { mutableStateOf(LocationHelper.hasPermission(ctx)) }
    var status by remember { mutableStateOf<String>("Ubicación no solicitada") }
    var city by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var bonusApplied by remember { mutableStateOf(false) } // Para deshabilitar tras reclamar

    // Launcher de permisos
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
        status = "Buscando..."
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

    // --- UI ESTILO DARK CASINO ---
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Encabezado (Icono + Título)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Gold)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Bono por ciudad",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    // Estado o Ciudad detectada
                    Text(
                        text = city?.let { "Estás en: $it" } ?: status,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            // 2. Zona de Acción (Botones)
            // Usamos una columna interna o Row condicional para evitar que se aplasten
            if (city != null && !bonusApplied) {
                // CASO: Ciudad encontrada -> Botón de reclamar GRANDE
                val base = currentAmount.coerceAtLeast(100)
                val bonus = (base * 20) / 100

                Button(
                    onClick = {
                        onApplyBonus(bonus)
                        bonusApplied = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reclamar +20% ($$bonus)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else if (!bonusApplied) {
                // CASO: Aún no hay ciudad -> Botón de detectar
                if (!hasPermission) {
                    OutlinedButton(
                        onClick = ::request,
                        enabled = !loading,
                        border = BorderStroke(1.dp, Gold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Dar Permiso")
                    }
                } else {
                    OutlinedButton(
                        onClick = { scope.launch { resolve() } },
                        enabled = !loading,
                        border = BorderStroke(1.dp, Gold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = Gold, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Buscando...")
                        } else {
                            Text("Detectar ubicación")
                        }
                    }
                }
            } else {
                // CASO: Ya reclamado
                Text(
                    "¡Bono aplicado con éxito!",
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}