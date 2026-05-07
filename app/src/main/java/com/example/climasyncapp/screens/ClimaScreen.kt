package com.example.climasyncapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climasyncapp.BluetoothManager
import com.example.climasyncapp.SensorData
import com.example.climasyncapp.parsearDatos
import com.example.climasyncapp.ui.theme.ClimaSyncAppTheme
import kotlinx.coroutines.*

@Composable
fun ClimaScreen(esPreview: Boolean = false) {
    val colors = MaterialTheme.colorScheme

    var ledOn        by remember { mutableStateOf(false) }
    var sensorActivo by remember { mutableStateOf(false) }
    var btConectado  by remember { mutableStateOf(false) }
    var statusText   by remember { mutableStateOf("Desconectado") }
    var sensorData   by remember { mutableStateOf(SensorData()) }

    // No crear BluetoothManager en preview
    val btManager = if (esPreview) null else remember { BluetoothManager() }
    val scope     = rememberCoroutineScope()

    if (!esPreview) {
        // ── Conexión automática al abrir ──────────────────────────────────────
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val device = btManager?.obtenerDispositivo("VERDOSO")
                if (device != null) {
                    withContext(Dispatchers.Main) { statusText = "Conectando..." }
                    val ok = btManager?.conectar(device) ?: false
                    withContext(Dispatchers.Main) {
                        if (ok) {
                            btConectado = true
                            statusText  = "Listo"
                        } else {
                            statusText = "Error Bluetooth"
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { statusText = "BT no encontrado" }
                }
            }
        }

        // ── Loop de lectura cuando sensor activo ──────────────────────────────
        LaunchedEffect(sensorActivo) {
            if (sensorActivo && btConectado) {
                withContext(Dispatchers.IO) {
                    while (sensorActivo) {
                        val linea = btManager?.leerLinea() ?: break
                        if (linea.startsWith("T:")) {
                            val datos = parsearDatos(linea)
                            if (datos != null) {
                                withContext(Dispatchers.Main) { sensorData = datos }
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Encabezado ───────────────────────────────────────────────────────
        Text(
            modifier   = Modifier.padding(top = 30.dp),
            text       = "ClimaSync",
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.onBackground
        )
        Text(
            text  = "Monitor de Clima",
            color = colors.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Tarjetas ─────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaSensor(
                modifier = Modifier.weight(1f),
                titulo   = "Temperatura",
                valor    = if (esPreview) "24.5" else if (sensorActivo) "${sensorData.temperatura}" else "--",
                unidad   = "°C"
            )
            TarjetaSensor(
                modifier = Modifier.weight(1f),
                titulo   = "Humedad",
                valor    = if (esPreview) "60.0" else if (sensorActivo) "${sensorData.humedad}" else "--",
                unidad   = "%"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Botón ON/OFF ──────────────────────────────────────────────────────
        Button(
            onClick = {
                if (btConectado) {
                    ledOn = !ledOn
                    scope.launch(Dispatchers.IO) {
                        btManager?.enviarComando("L")
                    }
                }
            },
            shape  = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor         = if (ledOn) Color(0xFF4CAF50) else Color(0xFFE53935),
                disabledContainerColor = Color(0xFF9E9E9E)
            ),
            modifier = Modifier.size(140.dp)
        ) {
            Text(
                text       = if (ledOn) "ON" else "OFF",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Estado ───────────────────────────────────────────────────────────
        Text(
            text  = if (esPreview) "Desconectado" else statusText,
            color = colors.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Botón Conectar/Desconectar sensor ─────────────────────────────────
        Button(
            onClick = {
                if (!sensorActivo) {
                    if (btConectado) {
                        scope.launch(Dispatchers.IO) { btManager?.enviarComando("C") }
                        sensorActivo = true
                        statusText   = "Sensor activo"
                    }
                } else {
                    scope.launch(Dispatchers.IO) { btManager?.enviarComando("D") }
                    sensorActivo = false
                    sensorData   = SensorData()
                    statusText   = "Sensor detenido"
                }
            },
            shape  = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sensorActivo) Color(0xFFE53935) else colors.primary
            )
        ) {
            Text(text = if (sensorActivo) "Desconectar" else "Conectarse")
        }
    }
}

// ── Tarjeta reutilizable ──────────────────────────────────────────────────────
@Composable
fun TarjetaSensor(
    modifier : Modifier = Modifier,
    titulo   : String,
    valor    : String,
    unidad   : String
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = titulo,
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment     = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text       = valor,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text     = unidad,
                    fontSize = 14.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ClimaScreenPreview() {
    ClimaSyncAppTheme {
        ClimaScreen(esPreview = true)
    }
}