package com.example.climasyncapp

data class SensorData(
    val temperatura: Float = 0f,
    val humedad: Float = 0f
)

// Parsea el string "T:25,H:60" que manda el PIC
fun parsearDatos(linea: String): SensorData? {
    return try {
        val partes = linea.split(",")
        val temp   = partes[0].removePrefix("T:").trim().toFloat()
        val hum    = partes[1].removePrefix("H:").trim().toFloat()
        SensorData(temperatura = temp, humedad = hum)
    } catch (e: Exception) { null }
}