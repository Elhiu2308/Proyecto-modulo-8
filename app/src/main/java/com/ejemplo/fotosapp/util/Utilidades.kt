package com.ejemplo.fotosapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Funciones auxiliares usadas por las pantallas. */
object Utilidades {

    /** Cifra la contraseña con SHA-256 para no guardarla en texto plano. */
    fun hashContrasena(contrasena: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** Formatea la fecha de la foto, por ejemplo: 19/10/2026 14:35. */
    fun formatearFecha(millis: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
        return formato.format(Date(millis))
    }

    /** Texto legible de la ubicación guardada. */
    fun textoUbicacion(latitud: Double?, longitud: Double?): String {
        if (latitud == null || longitud == null) return "Ubicación GPS no disponible"
        return String.format(Locale.US, "Lat: %.6f, Lon: %.6f", latitud, longitud)
    }

    /** Carpeta privada de la app donde se guardan las fotos: files/fotos/. */
    fun carpetaFotos(context: Context): File {
        val carpeta = File(context.filesDir, "fotos")
        if (!carpeta.exists()) carpeta.mkdirs()
        return carpeta
    }

    /**
     * Carga una foto reduciendo su tamaño para que no consuma demasiada memoria.
     * [anchoMaximo] se expresa en píxeles.
     */
    fun cargarBitmap(ruta: String, anchoMaximo: Int = 1080): Bitmap? {
        val archivo = File(ruta)
        if (!archivo.exists()) return null

        val opcionesLimites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, opcionesLimites)
        if (opcionesLimites.outWidth <= 0) return null

        var escala = 1
        while (opcionesLimites.outWidth / (escala * 2) >= anchoMaximo) escala *= 2

        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
        return BitmapFactory.decodeFile(ruta, opciones)
    }
}
