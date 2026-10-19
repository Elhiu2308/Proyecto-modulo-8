package com.ejemplo.fotosapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejemplo.fotosapp.databinding.ActivityRegistroBinding
import com.ejemplo.fotosapp.db.AppDatabase
import com.ejemplo.fotosapp.db.Usuario
import com.ejemplo.fotosapp.util.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla 1: Registro de usuario.
 * Pide nombre, apellidos, correo y contraseña, y los guarda en la base de datos local.
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener { registrar() }
        binding.tvIrLogin.setOnClickListener { finish() }
    }

    private fun registrar() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim().lowercase()
        val contrasena = binding.etContrasena.text.toString()
        val confirmar = binding.etConfirmar.text.toString()

        // Validaciones de los datos capturados.
        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(this, "El nombre y los apellidos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Escribe un correo válido", Toast.LENGTH_SHORT).show()
            return
        }
        if (contrasena.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        if (contrasena != confirmar) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegistrar.isEnabled = false

        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                val dao = AppDatabase.obtener(this@RegistroActivity).usuarioDao()
                if (dao.existeCorreo(correo) > 0) {
                    -2L // el correo ya está registrado
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
                    "Ese correo ya está registrado",
                    Toast.LENGTH_LONG
                ).show()

                resultado > 0L -> {
                    Toast.makeText(
                        this@RegistroActivity,
                        "Usuario registrado. Ya puedes iniciar sesión",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // regresa a la pantalla de inicio de sesión
                }

                else -> Toast.makeText(
                    this@RegistroActivity,
                    "No se pudo registrar el usuario",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
