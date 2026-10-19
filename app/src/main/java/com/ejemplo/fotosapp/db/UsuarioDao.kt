package com.ejemplo.fotosapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {

    /** Devuelve el id del usuario insertado, o -1 si el correo ya existía. */
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    /** Busca por correo (sin importar mayúsculas/minúsculas). */
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    /** Inicio de sesión: compara correo y contraseña cifrada. */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios WHERE correo = :correo")
    suspend fun existeCorreo(correo: String): Int

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?
}
