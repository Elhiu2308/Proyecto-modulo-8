package com.ejemplo.fotosapp.activities

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejemplo.fotosapp.R
import com.ejemplo.fotosapp.databinding.ActivityRegistroBinding
import com.ejemplo.fotosapp.models.Usuario
import com.ejemplo.fotosapp.services.AppDatabase
import com.ejemplo.fotosapp.services.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener { registrar() }
        binding.btnRegresar.setOnClickListener { finish() }
    }

    private fun registrar() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim().lowercase()
        val contrasena = binding.etContrasena.text.toString()
        val confirmar = binding.etConfirmar.text.toString()

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_error_nombre_obligatorio), Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, getString(R.string.msg_error_correo_valido), Toast.LENGTH_SHORT).show()
            return
        }
        if (contrasena.length < 6) {
            Toast.makeText(this, getString(R.string.msg_error_contrasena_corto), Toast.LENGTH_SHORT).show()
            return
        }
        if (contrasena != confirmar) {
            Toast.makeText(this, getString(R.string.msg_error_contraseñas_no_coinciden), Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegistrar.isEnabled = false

        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                val dao = AppDatabase.obtener(this@RegistroActivity).usuarioDao()
                if (dao.existeCorreo(correo) > 0) {
                    -2L
                } else {
                    dao.insertar(
                        Usuario(
                            nombre = nombre,
                            apellidos = apellidos,
                            correo = correo,
                            contrasena = Utilidades.hashContrasena(contrasena)
                        )
                    )
                }
            }
            binding.btnRegistrar.isEnabled = true

            when {
                resultado == -2L -> Toast.makeText(
                    this@RegistroActivity,
                    getString(R.string.msg_error_correo_registrado),
                    Toast.LENGTH_LONG
                ).show()

                resultado > 0L -> {
                    Toast.makeText(
                        this@RegistroActivity,
                        getString(R.string.msg_registro_exitoso),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                else -> Toast.makeText(
                    this@RegistroActivity,
                    getString(R.string.msg_error_registro_fallido),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
