package com.example.climasyncapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.climasyncapp.screens.ClimaScreen
import com.example.climasyncapp.ui.theme.ClimaSyncAppTheme

class MainActivity : ComponentActivity() {

    // Solicitud de permisos Bluetooth en tiempo de ejecución
    private val solicitarPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val todosOtorgados = permisos.values.all { it }
        if (!todosOtorgados) {
            // Si el usuario niega, volver a pedir (puedes mostrar un dialog aquí)
            pedirPermisosBluetooth()
        }
    }

    // Solicitud para activar Bluetooth si está apagado
    private val activarBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pedir permisos al iniciar
        pedirPermisosBluetooth()

        // Activar Bluetooth si está apagado
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            activarBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        setContent {
            ClimaSyncAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    ClimaScreen()
                }
            }
        }
    }

    private fun pedirPermisosBluetooth() {
        val permisosPendientes = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            if (!tienePermiso(Manifest.permission.BLUETOOTH_CONNECT))
                permisosPendientes.add(Manifest.permission.BLUETOOTH_CONNECT)
            if (!tienePermiso(Manifest.permission.BLUETOOTH_SCAN))
                permisosPendientes.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // Android 11 y anteriores
            if (!tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION))
                permisosPendientes.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permisosPendientes.isNotEmpty()) {
            solicitarPermisos.launch(permisosPendientes.toTypedArray())
        }
    }

    private fun tienePermiso(permiso: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED
    }
}