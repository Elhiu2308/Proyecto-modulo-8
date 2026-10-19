package com.ejemplo.fotosapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejemplo.fotosapp.databinding.ActivityLoginBinding
import com.ejemplo.fotosapp.db.AppDatabase
import com.ejemplo.fotosapp.util.SessionManager
import com.ejemplo.fotosapp.util.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla 2: Inicio de sesión.
 * Compara correo y contraseña contra la base de datos local.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)

        // Si ya hay una sesión guardada, entra directo a la pantalla principal.
        if (sesion.haySesion()) {
            abrirPrincipal()
            return
        }

        binding.btnIngresar.setOnClickListener { iniciarSesion() }
        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreo.text.toString().trim().lowercase()
        val contrasena = binding.etContrasena.text.toString()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Escribe tu correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnIngresar.isEnabled = false

        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                AppDatabase.obtener(this@LoginActivity).usuarioDao()
                    .iniciarSesion(correo, Utilidades.hashContrasena(contrasena))
            }
            binding.btnIngresar.isEnabled = true

            if (usuario != null) {
                sesion.iniciarSesion(usuario.id, usuario.nombre, usuario.correo)
                Toast.makeText(
                    this@LoginActivity,
                    "Bienvenido ${usuario.nombre}",
                    Toast.LENGTH_SHORT
                ).show()
                abrirPrincipal()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Usuario o contraseña incorrectos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun abrirPrincipal() {
        startActivity(Intent(this, PrincipalActivity::class.java))
        finish()
    }
}
