package com.ejemplo.fotosapp.activities

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
import com.ejemplo.fotosapp.R
import com.ejemplo.fotosapp.databinding.ActivityPrincipalBinding
import com.ejemplo.fotosapp.models.Foto
import com.ejemplo.fotosapp.services.AppDatabase
import com.ejemplo.fotosapp.services.SessionManager
import com.ejemplo.fotosapp.services.Utilidades
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var sesion: SessionManager

    private var fotoPendiente: File? = null

    private val clienteUbicacion by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val camara = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            mostrarFotoPendiente()
        } else {
            fotoPendiente?.delete()
            fotoPendiente = null
            Toast.makeText(this, getString(R.string.msg_foto_no_tomada), Toast.LENGTH_SHORT).show()
        }
    }

    private val permisosUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        actualizarCoordenadasGPS()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)
        binding.tvBienvenida.text = getString(R.string.hola_usuario, sesion.nombre())

        binding.btnTomarFoto.setOnClickListener { abrirCamara() }
        binding.btnGuardar.setOnClickListener { guardarFoto() }
        binding.btnNavGaleria.setOnClickListener {
            startActivity(Intent(this, VerFotosActivity::class.java))
        }
        binding.btnCerrarSesionToolbar.setOnClickListener { confirmarCerrarSesion() }

        solicitarPermisoUbicacion()
        actualizarCoordenadasGPS()
    }

    override fun onResume() {
        super.onResume()
        actualizarCoordenadasGPS()
    }

    private fun abrirCamara() {
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
        actualizarCoordenadasGPS()
        Toast.makeText(this, getString(R.string.msg_foto_lista_comentario), Toast.LENGTH_LONG).show()
    }

    private fun actualizarCoordenadasGPS() {
        lifecycleScope.launch {
            val ubicacion = obtenerUbicacion()
            if (ubicacion != null) {
                binding.tvCoordenadas.text = getString(
                    R.string.formato_coordenadas,
                    ubicacion.first,
                    ubicacion.second
                )
            } else {
                binding.tvCoordenadas.text = getString(R.string.msg_coordenadas_no_disponibles)
            }
        }
    }

    private fun guardarFoto() {
        val archivo = fotoPendiente
        if (archivo == null || !archivo.exists()) {
            Toast.makeText(this, getString(R.string.msg_primero_toma_foto), Toast.LENGTH_SHORT).show()
            return
        }
        val comentario = binding.etComentario.text.toString().trim()
        if (comentario.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_escribe_comentario), Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardar.isEnabled = false

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
                getString(R.string.msg_foto_guardada_sin_gps)
            else
                getString(R.string.msg_foto_guardada_con_gps, Utilidades.textoUbicacion(ubicacion.first, ubicacion.second))

            Toast.makeText(this@PrincipalActivity, textoGps, Toast.LENGTH_LONG).show()

            fotoPendiente = null
            binding.ivFoto.setImageDrawable(null)
            binding.etComentario.setText("")
            binding.btnGuardar.isEnabled = true
            actualizarCoordenadasGPS()
        }
    }

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
        } catch (_: SecurityException) {
            null
        }
    }

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

    private fun confirmarCerrarSesion() {
        Utilidades.mostrarDialogoConfirmacion(
            context = this,
            titulo = getString(R.string.btn_cerrar_sesion),
            mensaje = getString(R.string.msg_confirmar_cerrar_sesion),
            textoAceptar = getString(R.string.dialogo_si),
            iconoRes = R.drawable.logo_icono,
            alAceptar = { cerrarSesion() }
        )
    }

    private fun cerrarSesion() {
        sesion.cerrarSesion()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
