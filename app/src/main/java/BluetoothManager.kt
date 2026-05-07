package com.example.climasyncapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothManager {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun obtenerDispositivo(nombre: String = "VERDOSO"): BluetoothDevice? {
        return try {
            bluetoothAdapter?.bondedDevices?.find {
                it.name.contains(nombre, ignoreCase = true)
            }
        } catch (e: Exception) { null }
    }

    // Intenta conectar primero con UUID, si falla usa reflexión canal 1
    suspend fun conectar(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        // Intento 1: método estándar con UUID
        try {
            try { socket?.close() } catch (e: IOException) { }
            try { bluetoothAdapter?.cancelDiscovery() } catch (e: Exception) { }

            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket!!.connect()
            outputStream = socket!!.outputStream
            inputStream  = socket!!.inputStream
            android.util.Log.d("BT_CONN", "Conectado con UUID estándar")
            return@withContext true
        } catch (e: Exception) {
            android.util.Log.w("BT_CONN", "UUID falló, intentando reflexión: ${e.message}")
            try { socket?.close() } catch (e2: IOException) { }
        }

        // Intento 2: reflexión forzando canal 1 (funciona mejor con ZS-040/HC-05)
        try {
            try { bluetoothAdapter?.cancelDiscovery() } catch (e: Exception) { }

            val metodo = device.javaClass.getMethod("createRfcommSocket", Int::class.java)
            socket = metodo.invoke(device, 1) as BluetoothSocket
            socket!!.connect()
            outputStream = socket!!.outputStream
            inputStream  = socket!!.inputStream
            android.util.Log.d("BT_CONN", "Conectado con reflexión canal 1")
            return@withContext true
        } catch (e: Exception) {
            android.util.Log.e("BT_CONN", "Reflexión también falló: ${e.message}")
            try { socket?.close() } catch (e2: IOException) { }
            socket = null
            return@withContext false
        }
    }

    fun desconectar() {
        try { outputStream?.close() } catch (e: IOException) { }
        try { inputStream?.close()  } catch (e: IOException) { }
        try { socket?.close()       } catch (e: IOException) { }
        socket       = null
        outputStream = null
        inputStream  = null
    }

    fun enviarComando(cmd: String) {
        try {
            outputStream?.write(cmd.toByteArray())
            outputStream?.flush()
            android.util.Log.d("BT_CMD", "Enviado: $cmd")
        } catch (e: IOException) {
            android.util.Log.e("BT_CMD", "Error al enviar: ${e.message}")
        }
    }

    fun leerLinea(): String? {
        return try {
            val buffer = StringBuilder()
            val byte   = ByteArray(1)
            while (true) {
                val n = inputStream?.read(byte) ?: break
                if (n == -1) break
                val c = byte[0].toInt().toChar()
                if (c == '\n') break
                if (c != '\r') buffer.append(c)
            }
            val linea = buffer.toString()
            android.util.Log.d("BT_READ", "Recibido: $linea")
            linea
        } catch (e: IOException) {
            android.util.Log.e("BT_READ", "Error al leer: ${e.message}")
            null
        }
    }

    fun estaConectado(): Boolean = socket?.isConnected == true
}