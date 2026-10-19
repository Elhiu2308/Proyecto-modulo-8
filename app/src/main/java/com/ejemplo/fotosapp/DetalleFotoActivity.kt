package com.ejemplo.fotosapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejemplo.fotosapp.databinding.ActivityDetalleFotoBinding
import com.ejemplo.fotosapp.db.AppDatabase
import com.ejemplo.fotosapp.util.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Detalle de una foto: la imagen, el comentario y la ubicación GPS
 * que se registraron al guardarla.
 */
class DetalleFotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleFotoBinding
    private var latitud: Double? = null
    private var longitud: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fotoId = intent.getLongExtra(EXTRA_FOTO_ID, -1L)
        if (fotoId <= 0) {
            finish()
            return
        }

        binding.btnVerMapa.setOnClickListener { abrirMapa() }

        lifecycleScope.launch {
            val foto = withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@DetalleFotoActivity).fotoDao().buscarPorId(fotoId)
            }

            if (foto == null) {
                Toast.makeText(this@DetalleFotoActivity, "La foto ya no existe", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            latitud = foto.latitud
            longitud = foto.longitud

            binding.ivDetalle.setImageBitmap(Utilidades.cargarBitmap(foto.ruta))
            binding.tvComentario.text = foto.comentario
            binding.tvUbicacion.text = Utilidades.textoUbicacion(foto.latitud, foto.longitud)
            binding.tvFecha.text = "Tomada el ${Utilidades.formatearFecha(foto.fecha)}"
            binding.btnVerMapa.isEnabled = foto.latitud != null && foto.longitud != null
        }
    }

    /** Abre la ubicación GPS en una app de mapas. */
    private fun abrirMapa() {
        val lat = latitud ?: return
        val lon = longitud ?: return
        val intento = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:$lat,$lon?q=$lat,$lon")
        )
        try {
            startActivity(intento)
        } catch (e: Exception) {
            Toast.makeText(this, "No hay una app de mapas instalada", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_FOTO_ID = "extra_foto_id"
    }
}
