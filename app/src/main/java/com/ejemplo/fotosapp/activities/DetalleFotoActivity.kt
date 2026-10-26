package com.ejemplo.fotosapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejemplo.fotosapp.R
import com.ejemplo.fotosapp.databinding.ActivityDetalleFotoBinding
import com.ejemplo.fotosapp.models.Foto
import com.ejemplo.fotosapp.services.AppDatabase
import com.ejemplo.fotosapp.services.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DetalleFotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleFotoBinding
    private var fotoActual: Foto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fotoId = intent.getLongExtra(EXTRA_FOTO_ID, -1L)
        if (fotoId <= 0) {
            finish()
            return
        }

        binding.btnRegresar.setOnClickListener { finish() }
        binding.btnVerMapa.setOnClickListener { abrirMapa() }
        binding.btnEliminar.setOnClickListener { confirmarEliminar() }

        lifecycleScope.launch {
            val foto = withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@DetalleFotoActivity).fotoDao().buscarPorId(fotoId)
            }

            if (foto == null) {
                Toast.makeText(this@DetalleFotoActivity, getString(R.string.msg_foto_no_existe), Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            fotoActual = foto

            binding.ivDetalle.setImageBitmap(Utilidades.cargarBitmap(foto.ruta))
            binding.tvComentario.text = foto.comentario
            binding.tvUbicacion.text = Utilidades.textoUbicacion(foto.latitud, foto.longitud)
            binding.tvFecha.text = getString(R.string.msg_tomada_el, Utilidades.formatearFecha(foto.fecha))
            binding.btnVerMapa.isEnabled = foto.latitud != null && foto.longitud != null
        }
    }

    private fun confirmarEliminar() {
        val foto = fotoActual ?: return
        Utilidades.mostrarDialogoConfirmacion(
            context = this,
            titulo = getString(R.string.titulo_confirmar_eliminar),
            mensaje = getString(R.string.msg_confirmar_eliminar_foto),
            textoAceptar = getString(R.string.btn_eliminar),
            esAccionPeligrosa = true,
            iconoRes = R.drawable.ic_delete,
            alAceptar = { eliminarFoto(foto) }
        )
    }

    private fun eliminarFoto(foto: Foto) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@DetalleFotoActivity).fotoDao().eliminar(foto)
                try {
                    File(foto.ruta).delete()
                } catch (_: Exception) {
                }
            }
            Toast.makeText(this@DetalleFotoActivity, getString(R.string.msg_foto_eliminada), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun abrirMapa() {
        val foto = fotoActual ?: return
        val lat = foto.latitud ?: return
        val lon = foto.longitud ?: return
        val intento = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:$lat,$lon?q=$lat,$lon")
        )
        try {
            startActivity(intento)
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.msg_no_app_mapas), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_FOTO_ID = "extra_foto_id"
    }
}
