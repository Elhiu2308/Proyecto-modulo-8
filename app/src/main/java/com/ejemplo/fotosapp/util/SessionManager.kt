package com.ejemplo.fotosapp.util

import android.content.Context

/**
 * Guarda la sesión del usuario en SharedPreferences, para no pedir
 * la contraseña cada vez que se abre la app.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun iniciarSesion(usuarioId: Long, nombre: String, correo: String) {
        prefs.edit()
            .putLong(CLAVE_ID, usuarioId)
            .putString(CLAVE_NOMBRE, nombre)
            .putString(CLAVE_CORREO, correo)
            .apply()
    }

    fun cerrarSesion() = prefs.edit().clear().apply()

    fun usuarioId(): Long = prefs.getLong(CLAVE_ID, -1L)

    fun nombre(): String = prefs.getString(CLAVE_NOMBRE, "") ?: ""

    fun correo(): String = prefs.getString(CLAVE_CORREO, "") ?: ""

    fun haySesion(): Boolean = usuarioId() > 0

    companion object {
        private const val CLAVE_ID = "usuario_id"
        private const val CLAVE_NOMBRE = "usuario_nombre"
        private const val CLAVE_CORREO = "usuario_correo"
    }
}
