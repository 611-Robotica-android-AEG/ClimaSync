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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.climasyncapp.screens.SentryScreen
import com.example.climasyncapp.ui.theme.ClimaSyncAppTheme

class SentryActivity : ComponentActivity() {

    private val solicitarPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (!permisos.values.all { it }) pedirPermisosBluetooth()
    }

    private val activarBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pedirPermisosBluetooth()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            activarBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        setContent {
            ClimaSyncAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    SentryScreen()
                }
            }
        }
    }

    private fun pedirPermisosBluetooth() {
        val pendientes = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!tienePermiso(Manifest.permission.BLUETOOTH_CONNECT))
                pendientes.add(Manifest.permission.BLUETOOTH_CONNECT)
            if (!tienePermiso(Manifest.permission.BLUETOOTH_SCAN))
                pendientes.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            if (!tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION))
                pendientes.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (pendientes.isNotEmpty()) solicitarPermisos.launch(pendientes.toTypedArray())
    }

    private fun tienePermiso(permiso: String) =
        ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED
}