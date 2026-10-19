package com.ejemplo.fotosapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.ejemplo.fotosapp.databinding.ActivityPrincipalBinding
import com.ejemplo.fotosapp.db.AppDatabase
import com.ejemplo.fotosapp.db.Foto
import com.ejemplo.fotosapp.util.SessionManager
import com.ejemplo.fotosapp.util.Utilidades
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Pantalla 3: Pantalla principal.
 * Ofrece las opciones de TOMAR FOTO y VER FOTOS. Después de tomar la foto la
 * muestra, permite escribir un comentario y la guarda (archivo + GPS + comentario).
 */
class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var sesion: SessionManager

    /** Archivo de la última foto tomada, todavía sin guardar en la base de datos. */
    private var fotoPendiente: File? = null

    private val clienteUbicacion by lazy { LocationServices.getFusedLocationProviderClient(this) }

    /** Resultado de abrir la cámara. */
    private val camara = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            mostrarFotoPendiente()
        } else {
            fotoPendiente?.delete()
            fotoPendiente = null
            Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val permisosUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* La lectura de la ubicación se intenta de nuevo al guardar. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)
        binding.tvBienvenida.text = "Hola, ${sesion.nombre()}"

        binding.btnTomarFoto.setOnClickListener { abrirCamara() }
        binding.btnGuardar.setOnClickListener { guardarFoto() }
        binding.btnVerFotos.setOnClickListener {
            startActivity(Intent(this, VerFotosActivity::class.java))
        }
        binding.btnCerrarSesion.setOnClickListener { cerrarSesion() }

        solicitarPermisoUbicacion()
        actualizarControles()
    }

    // ---------------------------------------------------------------------
    // Tomar la foto
    // ---------------------------------------------------------------------

    private fun abrirCamara() {
        // La foto se guarda directamente en la carpeta privada de la app: files/fotos/
        val archivo = File(Utilidades.carpetaFotos(this), "foto_${System.currentTimeMillis()}.jpg")
        fotoPendiente = archivo

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            archivo
        )
        camara.launch(uri)
    }

    private fun mostrarFotoPendiente() {
        val archivo = fotoPendiente ?: return
        binding.ivFoto.setImageBitmap(Utilidades.cargarBitmap(archivo.absolutePath))
        binding.etComentario.setText("")
        actualizarControles()
        Toast.makeText(this, "Foto lista. Escribe un comentario y guárdala", Toast.LENGTH_LONG).show()
    }

    // ---------------------------------------------------------------------
    // Guardar la foto en la base de datos (ruta + comentario + GPS)
    // ---------------------------------------------------------------------

    private fun guardarFoto() {
        val archivo = fotoPendiente
        if (archivo == null || !archivo.exists()) {
            Toast.makeText(this, "Primero toma una foto", Toast.LENGTH_SHORT).show()
            return
        }
        val comentario = binding.etComentario.text.toString().trim()
        if (comentario.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario para la foto", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardar.isEnabled = false
        binding.tvEstado.text = "Obteniendo ubicación GPS..."

        lifecycleScope.launch {
            val ubicacion = obtenerUbicacion()
            val usuarioId = sesion.usuarioId()

            withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@PrincipalActivity).fotoDao().insertar(
                    Foto(
                        usuarioId = usuarioId,
                        ruta = archivo.absolutePath,
                        comentario = comentario,
                        latitud = ubicacion?.first,
                        longitud = ubicacion?.second,
                        fecha = System.currentTimeMillis()
                    )
                )
            }

            val textoGps = if (ubicacion == null)
                "Foto guardada sin ubicación GPS"
            else
                "Foto guardada con ubicación ${Utilidades.textoUbicacion(ubicacion.first, ubicacion.second)}"

            Toast.makeText(this@PrincipalActivity, textoGps, Toast.LENGTH_LONG).show()
            binding.tvEstado.text = textoGps

            // Se limpia la pantalla para poder tomar otra foto.
            fotoPendiente = null
            binding.ivFoto.setImageDrawable(null)
            binding.etComentario.setText("")
            actualizarControles()
        }
    }

    /**
     * Devuelve la última ubicación conocida como par (latitud, longitud),
     * o null si no hay permiso o no hay señal GPS.
     */
    private suspend fun obtenerUbicacion(): Pair<Double, Double>? {
        if (!tienePermisoUbicacion()) return null
        return try {
            suspendCancellableCoroutine { continuacion ->
                clienteUbicacion.lastLocation
                    .addOnSuccessListener { ubicacion ->
                        continuacion.resume(
                            if (ubicacion == null) null else ubicacion.latitude to ubicacion.longitude
                        )
                    }
                    .addOnFailureListener { continuacion.resume(null) }
            }
        } catch (e: SecurityException) {
            null
        }
    }

    // ---------------------------------------------------------------------
    // Permisos y utilidades de la pantalla
    // ---------------------------------------------------------------------

    private fun tienePermisoUbicacion(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun solicitarPermisoUbicacion() {
        if (!tienePermisoUbicacion()) {
            permisosUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun actualizarControles() {
        val hayFoto = fotoPendiente?.exists() == true
        binding.btnGuardar.isEnabled = hayFoto
        binding.tvEstado.text = if (hayFoto) {
            "Foto lista para guardar"
        } else {
            "Toma una foto para comenzar"
        }
    }

    private fun cerrarSesion() {
        sesion.cerrarSesion()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
