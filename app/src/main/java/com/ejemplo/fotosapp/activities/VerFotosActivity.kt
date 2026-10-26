package com.ejemplo.fotosapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ejemplo.fotosapp.R
import com.ejemplo.fotosapp.adapters.FotoAdapter
import com.ejemplo.fotosapp.databinding.ActivityVerFotosBinding
import com.ejemplo.fotosapp.models.Foto
import com.ejemplo.fotosapp.services.AppDatabase
import com.ejemplo.fotosapp.services.SessionManager
import com.ejemplo.fotosapp.services.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VerFotosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerFotosBinding
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerFotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)
        binding.recyclerFotos.layoutManager = LinearLayoutManager(this)

        binding.btnRegresar.setOnClickListener { finish() }
        binding.btnNavInicio.setOnClickListener { finish() }

        cargarFotos()
    }

    override fun onResume() {
        super.onResume()
        cargarFotos()
    }

    private fun cargarFotos() {
        lifecycleScope.launch {
            val fotos = withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@VerFotosActivity).fotoDao()
                    .listarPorUsuario(sesion.usuarioId())
            }

            if (fotos.isEmpty()) {
                binding.tvVacio.visibility = View.VISIBLE
                binding.recyclerFotos.visibility = View.GONE
            } else {
                binding.tvVacio.visibility = View.GONE
                binding.recyclerFotos.visibility = View.VISIBLE
                binding.recyclerFotos.adapter = FotoAdapter(
                    fotos = fotos,
                    alSeleccionar = { foto ->
                        startActivity(
                            Intent(this@VerFotosActivity, DetalleFotoActivity::class.java)
                                .putExtra(DetalleFotoActivity.EXTRA_FOTO_ID, foto.id)
                        )
                    },
                    alEliminar = { foto ->
                        confirmarEliminarFoto(foto)
                    }
                )
            }
        }
    }

    private fun confirmarEliminarFoto(foto: Foto) {
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
                AppDatabase.obtener(this@VerFotosActivity).fotoDao().eliminar(foto)
                try {
                    File(foto.ruta).delete()
                } catch (_: Exception) {
                }
            }
            Toast.makeText(this@VerFotosActivity, getString(R.string.msg_foto_eliminada), Toast.LENGTH_SHORT).show()
            cargarFotos()
        }
    }
}
