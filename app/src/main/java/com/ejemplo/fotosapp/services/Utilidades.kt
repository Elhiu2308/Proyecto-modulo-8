package com.ejemplo.fotosapp.services

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.ejemplo.fotosapp.R
import com.google.android.material.button.MaterialButton
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utilidades {

    fun hashContrasena(contrasena: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun formatearFecha(millis: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
        return formato.format(Date(millis))
    }

    fun textoUbicacion(latitud: Double?, longitud: Double?): String {
        if (latitud == null || longitud == null) return "Ubicación GPS no disponible"
        return String.format(Locale.US, "Lat: %.6f, Lon: %.6f", latitud, longitud)
    }

    fun carpetaFotos(context: Context): File {
        val carpeta = File(context.filesDir, "fotos")
        if (!carpeta.exists()) carpeta.mkdirs()
        return carpeta
    }

    fun cargarBitmap(ruta: String, anchoMaximo: Int = 1080): Bitmap? {
        val archivo = File(ruta)
        if (!archivo.exists()) return null

        val opcionesLimites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, opcionesLimites)
        if (opcionesLimites.outWidth <= 0 || opcionesLimites.outHeight <= 0) return null

        val dimensionMax = maxOf(opcionesLimites.outWidth, opcionesLimites.outHeight)
        var escala = 1
        while (dimensionMax / (escala * 2) >= anchoMaximo) escala *= 2

        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
        val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

        return rotarBitmapSegunExif(ruta, bitmap)
    }

    private fun rotarBitmapSegunExif(ruta: String, bitmap: Bitmap): Bitmap {
        val grados = try {
            val exif = ExifInterface(ruta)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }

        if (grados == 0) return bitmap

        val matrix = Matrix().apply { postRotate(grados.toFloat()) }
        val bitmapRotado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (bitmapRotado != bitmap) {
            bitmap.recycle()
        }
        return bitmapRotado
    }

    fun mostrarDialogoConfirmacion(
        context: Context,
        titulo: String,
        mensaje: String,
        textoAceptar: String,
        textoCancelar: String = context.getString(R.string.dialogo_no),
        esAccionPeligrosa: Boolean = false,
        iconoRes: Int = R.drawable.logo_icono,
        alAceptar: () -> Unit
    ) {
        val vista = LayoutInflater.from(context).inflate(R.layout.dialog_confirmacion, null)
        val dialog = AlertDialog.Builder(context)
            .setView(vista)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val ivIcono = vista.findViewById<ImageView>(R.id.ivIconoDialogo)
        val tvTitulo = vista.findViewById<TextView>(R.id.tvTituloDialogo)
        val tvMensaje = vista.findViewById<TextView>(R.id.tvMensajeDialogo)
        val btnCancelar = vista.findViewById<MaterialButton>(R.id.btnCancelarDialogo)
        val btnAceptar = vista.findViewById<MaterialButton>(R.id.btnAceptarDialogo)

        ivIcono.setImageResource(iconoRes)
        tvTitulo.text = titulo
        tvMensaje.text = mensaje
        btnCancelar.text = textoCancelar
        btnAceptar.text = textoAceptar

        if (esAccionPeligrosa) {
            btnAceptar.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.rojo_error)
            )
        } else {
            btnAceptar.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.azul_primario)
            )
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnAceptar.setOnClickListener {
            dialog.dismiss()
            alAceptar()
        }

        dialog.show()
    }
}
