package com.ejemplo.fotosapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ejemplo.fotosapp.databinding.ActivityVerFotosBinding
import com.ejemplo.fotosapp.db.AppDatabase
import com.ejemplo.fotosapp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla 5: Ver fotos.
 * Consulta la lista de fotos en la base de datos. Al seleccionar una se
 * muestra el detalle con su comentario y su ubicación GPS.
 */
class VerFotosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerFotosBinding
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerFotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)
        binding.recyclerFotos.layoutManager = LinearLayoutManager(this)

        cargarFotos()
    }

    private fun cargarFotos() {
        lifecycleScope.launch {
            val fotos = withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@VerFotosActivity).fotoDao()
                    .listarPorUsuario(sesion.usuarioId())
            }

            if (fotos.isEmpty()) {
                binding.tvVacio.visibility = android.view.View.VISIBLE
                binding.recyclerFotos.visibility = android.view.View.GONE
            } else {
                binding.tvVacio.visibility = android.view.View.GONE
                binding.recyclerFotos.visibility = android.view.View.VISIBLE
                binding.recyclerFotos.adapter = FotoAdapter(fotos) { foto ->
                    startActivity(
                        Intent(this@VerFotosActivity, DetalleFotoActivity::class.java)
                            .putExtra(DetalleFotoActivity.EXTRA_FOTO_ID, foto.id)
                    )
                }
            }
        }
    }
}
