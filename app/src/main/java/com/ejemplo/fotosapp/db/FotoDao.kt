package com.ejemplo.fotosapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FotoDao {

    @Insert
    suspend fun insertar(foto: Foto): Long

    @Delete
    suspend fun eliminar(foto: Foto)

    /** Lista de fotos del usuario, la más reciente primero. */
    @Query("SELECT * FROM fotos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    suspend fun listarPorUsuario(usuarioId: Long): List<Foto>

    @Query("SELECT * FROM fotos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Foto?

    @Query("SELECT COUNT(*) FROM fotos WHERE usuarioId = :usuarioId")
    suspend fun contarPorUsuario(usuarioId: Long): Int
}
