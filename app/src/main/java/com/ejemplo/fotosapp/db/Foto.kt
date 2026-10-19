package com.ejemplo.fotosapp.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Foto tomada por el usuario.
 *
 * - [ruta] es la ubicación del archivo JPG dentro de la carpeta privada de la app
 *   (`files/fotos/`), no es una ruta pública.
 * - [latitud] y [longitud] guardan la ubicación GPS del momento en que se guardó.
 */
@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Foto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val ruta: String,
    val comentario: String,
    val latitud: Double?,
    val longitud: Double?,
    val fecha: Long
)
