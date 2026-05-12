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
import com.example.climasyncapp.ui.theme.ClimaSyncAppTheme
import kotlinx.coroutines.*

@Composable
fun SentryScreen(esPreview: Boolean = false) {

    val colorFondo = Color(0xFFF4A7A7)

    var btConectado  by remember { mutableStateOf(false) }
    var alarmaActiva by remember { mutableStateOf(false) }
    var movimiento   by remember { mutableStateOf(false) }
    var statusText   by remember { mutableStateOf("Desconectado") }

    val btManager = if (esPreview) null else remember { BluetoothManager() }
    val scope     = rememberCoroutineScope()

    if (!esPreview) {
        LaunchedEffect(btConectado) {
            if (btConectado) {
                withContext(Dispatchers.IO) {
                    while (btConectado) {
                        val linea = btManager?.leerLinea() ?: break
                        when (linea.trim()) {
                            "M:1" -> withContext(Dispatchers.Main) { movimiento = true  }
                            "M:0" -> withContext(Dispatchers.Main) { movimiento = false }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(colorFondo),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text       = "Sentry",
            fontSize   = 36.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
        Text(
            text     = "Detector de movimiento",
            fontSize = 14.sp,
            color    = Color.Black.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    color = if (movimiento) Color(0xFFCC3333) else Color(0xFF9E9E9E),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text     = "LED rojo",
            fontSize = 13.sp,
            color    = Color.Black.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Caja de estado centrada ───────────────────────────────────────────
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier            = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text     = "Estado",
                    fontSize = 12.sp,
                    color    = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = if (movimiento) "MOVIMIENTO DETECTADO" else "Sin movimiento",
                    fontSize   = if (movimiento) 18.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (movimiento) Color(0xFFCC0000) else Color(0xFF2E7D32),
                    fontFamily = if (movimiento)
                        androidx.compose.ui.text.font.FontFamily.Monospace
                    else
                        androidx.compose.ui.text.font.FontFamily.Default
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (btConectado) {
                    alarmaActiva = !alarmaActiva
                    if (!alarmaActiva) movimiento = false
                    scope.launch(Dispatchers.IO) {
                        btManager?.enviarComando(if (alarmaActiva) "1" else "0")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor         = if (alarmaActiva) Color(0xFFCC0000) else Color(0xFF1A237E),
                disabledContainerColor = Color(0xFF9E9E9E)
            )
        ) {
            Text(
                text       = if (alarmaActiva) "DESACTIVAR" else "Activar alarma",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text     = if (esPreview) "Desconectado" else statusText,
            fontSize = 13.sp,
            color    = Color.Black.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (!btConectado) {
                    scope.launch {
                        statusText = "Conectando..."
                        val device = withContext(Dispatchers.IO) {
                            btManager?.obtenerDispositivo("VERDOSO")
                        }
                        if (device != null) {
                            val ok = withContext(Dispatchers.IO) {
                                btManager?.conectar(device) ?: false
                            }
                            if (ok) {
                                btConectado = true
                                statusText  = "Conectado"
                            } else {
                                statusText = "Error al conectar"
                            }
                        } else {
                            statusText = "Dispositivo no encontrado"
                        }
                    }
                } else {
                    btManager?.desconectar()
                    btConectado  = false
                    alarmaActiva = false
                    movimiento   = false
                    statusText   = "Desconectado"
                }
            },
            modifier = Modifier
                .padding(horizontal = 80.dp)
                .fillMaxWidth(),
            shape  = RoundedCornerShape(20.dp),
            colors = ButtonColors(
                containerColor         = Color.Transparent,
                contentColor           = Color.Black,
                disabledContainerColor = Color.Transparent,
                disabledContentColor   = Color.Gray
            )
        ) {
            Text(
                text  = if (btConectado) "Desconectarse" else "Conectarse",
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SentryScreenPreview() {
    ClimaSyncAppTheme {
        SentryScreen(esPreview = true)
    }
}