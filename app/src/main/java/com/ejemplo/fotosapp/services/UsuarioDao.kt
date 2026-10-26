package com.ejemplo.fotosapp.services

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ejemplo.fotosapp.models.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios WHERE correo = :correo")
    suspend fun existeCorreo(correo: String): Int

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?
}
